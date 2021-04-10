/*
 * Copyright 2020 Andrew Rice <acr31@cam.ac.uk>, Alastair Beresford <arb33@cam.ac.uk>, C.I. Griffiths
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.cam.cig23.fjava.tick5;


import uk.ac.cam.cl.fjava.messages.*;

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class ChatClient {




    public static void main(String[] args) {
        String server = null;
        int port = 0;
        String id = java.util.UUID.randomUUID().toString();
        VectorClock clk = new VectorClock();

        if(args.length != 2){
            System.err.println("This application requires two arguments: <machine> <port>");
            return;
        } else {
            try {
                server = args[0];
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException ex) {
                System.err.println("This application requires two arguments: <machine> <port>");
                return;
            }
        }

        final Socket s;
        try {
            s = new Socket(server, port);
            DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
            System.out.println(dateFormat.format(new Date())+" [Client] Connected to "+server+" on port "+port+".");
        } catch (java.io.IOException ex) {
            System.err.format("Cannot connect to %s on port %s\n", server, port);
            return;
        }
        Thread output =
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            InputStream input = s.getInputStream();
                            DynamicObjectInputStream inputStream = new DynamicObjectInputStream(input);
                            ReorderBuffer buf = null;
                            boolean first = true;
                            while(true) {

                                Message message = (Message) inputStream.readObject();
                                clk.updateClock(message.getVectorClock());
                                if(first) {
                                    buf = new ReorderBuffer(message.getVectorClock());
                                    first=false;
                                    if (message instanceof RelayMessage) {
                                        RelayMessage myMessage = (RelayMessage) message;
                                        //print time [name] Message
                                        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
                                        System.out.println(dateFormat.format(myMessage.getCreationTime()) + " [" + myMessage.getFrom() + "] " + myMessage.getMessage());
                                    } else if (message instanceof StatusMessage) {
                                        StatusMessage myMessage = (StatusMessage) message;
                                        //print time [Server] Message
                                        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
                                        System.out.println(dateFormat.format(myMessage.getCreationTime()) + " [Server] " + myMessage.getMessage());
                                    } else if (message instanceof NewMessageType) {
                                        NewMessageType newMessage = (NewMessageType) message;
                                        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");


                                        System.out.println(dateFormat.format(newMessage.getCreationTime()) + " [Client] New class "+newMessage.getName()+" loaded." );
                                        inputStream.addClass(newMessage.getName(),newMessage.getClassData());
                                    } else {
                                        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
                                        Class<?> newClass = message.getClass();
                                        Field[] declaredFields = newClass.getDeclaredFields();
                                        Method[] methods = newClass.getDeclaredMethods();
                                        List<Method> runMe = new ArrayList<Method>();
                                        for (Method method : methods) {
                                            Class[] parameterTypes = method.getParameterTypes();
                                            Annotation[] annotations = method.getAnnotations();
                                            if(parameterTypes.length == 0) {
                                                for(Annotation annotation : annotations) {
                                                    if (annotation instanceof Execute) {
                                                        runMe.add(method);
                                                    }
                                                }
                                            }
                                        }
                                        String created = ": ";
                                        Object myMessage = message;
                                        boolean flag = false;
                                        for (Field item : declaredFields) {
                                            item.setAccessible(true);
                                            flag=true;
                                            try {
                                                created += item.getName() + "(" + item.get(myMessage) + ")" + ", ";
                                            } catch (IllegalAccessException ex) {
                                                System.exit(0);
                                            }
                                        }
                                        if(flag) {
                                            created=created.substring(0, created.length() - 2);
                                        }
                                        System.out.println(dateFormat.format(new Date()) + " [Client] "+newClass.getSimpleName()+""+created );
                                        for(Method method : runMe) {
                                            try {
                                                method.invoke(myMessage);
                                            } catch (IllegalAccessException | InvocationTargetException ex) {
                                                System.exit(0);
                                            }
                                        }
                                    }
                                } else {
                                    buf.addMessage(message);
                                    clk.updateClock(message.getVectorClock());
                                    Collection<Message> valid = buf.pop();
                                    if (valid!=null) {
                                        for (Message m : valid) {
                                            if (m instanceof RelayMessage) {
                                                RelayMessage myMessage = (RelayMessage) m;
                                                //print time [name] Message
                                                DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
                                                System.out.println(dateFormat.format(myMessage.getCreationTime()) + " [" + myMessage.getFrom() + "] " + myMessage.getMessage());
                                            } else if (m instanceof StatusMessage) {
                                                StatusMessage myMessage = (StatusMessage) m;
                                                //print time [Server] Message
                                                DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
                                                System.out.println(dateFormat.format(myMessage.getCreationTime()) + " [Server] " + myMessage.getMessage());
                                            } else if (m instanceof NewMessageType) {
                                                NewMessageType newMessage = (NewMessageType) m;
                                                DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");


                                                System.out.println(dateFormat.format(newMessage.getCreationTime()) + " [Client] New class " + newMessage.getName() + " loaded.");
                                                inputStream.addClass(newMessage.getName(), newMessage.getClassData());
                                            } else {
                                                DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
                                                Class<?> newClass = m.getClass();
                                                Field[] declaredFields = newClass.getDeclaredFields();
                                                Method[] methods = newClass.getDeclaredMethods();
                                                List<Method> runMe = new ArrayList<Method>();
                                                for (Method method : methods) {
                                                    Class[] parameterTypes = method.getParameterTypes();
                                                    Annotation[] annotations = method.getAnnotations();
                                                    if (parameterTypes.length == 0) {
                                                        for (Annotation annotation : annotations) {
                                                            if (annotation instanceof Execute) {
                                                                runMe.add(method);
                                                            }
                                                        }
                                                    }
                                                }
                                                String created = ": ";
                                                Object myMessage = m;
                                                boolean flag = false;
                                                for (Field item : declaredFields) {
                                                    item.setAccessible(true);
                                                    flag = true;
                                                    try {
                                                        created += item.getName() + "(" + item.get(myMessage) + ")" + ", ";
                                                    } catch (IllegalAccessException ex) {
                                                        System.exit(0);
                                                    }
                                                }
                                                if (flag) {
                                                    created = created.substring(0, created.length() - 2);
                                                }
                                                System.out.println(dateFormat.format(new Date()) + " [Client] " + newClass.getSimpleName() + "" + created);
                                                for (Method method : runMe) {
                                                    try {
                                                        method.invoke(myMessage);
                                                    } catch (IllegalAccessException | InvocationTargetException ex) {
                                                        System.exit(0);
                                                    }
                                                }
                                            }

                                        }
                                    }
                                }
                            }
                        } catch (ClassNotFoundException ex) {
                            DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
                            System.out.println(dateFormat.format(new Date()) + " [Client] New message of unknown type received." );
                        } catch (IOException ex) {
                            System.err.println(ex);
                            return;
                        }

                    }
                };
        output.setDaemon(true);
        output.start();
        BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
        try {
            OutputStream input = s.getOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(input);
            while (true) {

                String str = r.readLine();
                //if str starts with \ do command stuff
                if(str.startsWith("\\")) {
                    if(str.startsWith("\\nick")) {
                        //remove nick message and send a ChangeNickMessage to server
                        str = str.replace("\\nick ", "");
                        ChangeNickMessage newMessage = new ChangeNickMessage(str,id,clk.incrementClock(id));
                        out.writeObject(newMessage);
                    } else if (str.startsWith("\\quit")) {
                        //print quitting message and quit
                        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
                        System.out.format("%s [Client] Connection terminated.\n", dateFormat.format(new Date()));
                        System.exit(0);
                    } else {
                        //display command not recognised
                        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
                        System.out.println(dateFormat.format(new Date()) +" [Client] Unknown command \""+str.replace("\\","")+"\"");
                    }
                } else {
                    //else send normal message
                    ChatMessage myMessage = new ChatMessage(str,id,clk.incrementClock(id));
                    out.writeObject(myMessage);
                }

            }
        } catch (IOException ex) {
            System.err.println(ex);
            return;
        }
    }

}
