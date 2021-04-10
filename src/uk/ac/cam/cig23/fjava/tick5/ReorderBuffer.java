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

import java.util.*;

import uk.ac.cam.cl.fjava.messages.Message;

public class ReorderBuffer {

  private final List<Message> buffer;
  private final VectorClock lastDisplayed;

  public void addMessage(Message m) {

    buffer.add(m);
  }

  public ReorderBuffer(Map<String, Integer> initialMsg) {

    buffer = new ArrayList<>();
    lastDisplayed = new VectorClock(initialMsg);
  }

  public Collection<Message> pop() {

    ArrayList<Message> elig = null;
    boolean addedOne = true;
    while(addedOne) {
      addedOne=false;
      for (Message m : buffer) {
        Map<String, Integer> mVecClk = m.getVectorClock();
        Set<String> keys = mVecClk.keySet();
        int howManyOneGreater = 0;
        int howManyLeq = 0;
        for (String key : keys) {
          Integer mVal = mVecClk.get(key);
          Integer curVal = lastDisplayed.getClock(key);
          if (mVal <= curVal) {
            howManyLeq++;
          } else if (mVal == curVal + 1) {
            howManyOneGreater++;
          }
        }
        if (howManyOneGreater == 1 && howManyLeq == keys.size() - 1) {
          if (elig == null) {
            elig = new ArrayList<>();
          }
          addedOne=true;
          elig.add(m);
          lastDisplayed.updateClock(mVecClk);
        }
      }
      if (elig != null) {
        for (Message m : elig) {
          buffer.remove(m);
        }
      }
    }

    return elig;
  }
}
