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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class VectorClock {

  private final Map<String, Integer> vectorClock;

  public VectorClock() {
    vectorClock = new HashMap<>();
  }

  public VectorClock(Map<String, Integer> existingClock) {
    vectorClock = new HashMap<>(existingClock);
  }

  public synchronized void updateClock(Map<String, Integer> msgClock) {

    Map<String, Integer> union = new HashMap<String, Integer>();
    union.putAll(msgClock);
    union.putAll(vectorClock);
    for (String key : union.keySet()) {
      vectorClock.put(key, Math.max(vectorClock.getOrDefault(key,0),msgClock.getOrDefault(key,0)));
    }
  }

  public synchronized Map<String, Integer> incrementClock(String uid) {

    vectorClock.put(uid, vectorClock.getOrDefault(uid,0)+1);
    // Return a copy of the contents of the clock
    return new HashMap<>(vectorClock);

    //We will assign message clocks equal to this incremented clock. If we pass the clock
    //itself then the client that receives our message can modify the value of our clock
    //when it modifies the message clock rather than just a copy of our clock.
  }

  public synchronized int getClock(String uid) {

    return vectorClock.getOrDefault(uid,0);
  }

  public synchronized boolean happenedBefore(Map<String, Integer> other) {

    boolean oneSmaller = false;
    boolean allLessOrEq = true;
    Map<String, Integer> union = new HashMap<String, Integer>();
    union.putAll(other);
    union.putAll(vectorClock);
    for (String key : union.keySet()) {
      Integer clkVal = vectorClock.getOrDefault(key,0);
      Integer otherVal = other.getOrDefault(key,0);
      if (clkVal<otherVal) {
        oneSmaller = true;
      } else if (clkVal>otherVal) {
        allLessOrEq=false;
      }
    }
    return oneSmaller && allLessOrEq;
  }
}
