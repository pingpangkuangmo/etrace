/*
 * Copyright 2019 etrace.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.etrace.agent.monitor.jvm;

import io.etrace.agent.monitor.HeartBeatConstants;
import io.etrace.agent.monitor.HeartBeatExecutor;

import java.lang.management.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class JvmThreadHeartBeatExecutor extends HeartBeatExecutor {
    private static final int MAX_FRAMES = 20;
    private boolean dumpLocked;

    public JvmThreadHeartBeatExecutor(String type) {
        super(type);
    }

    public void setDumpLocked(boolean dumpLocked) {
        this.dumpLocked = dumpLocked;
    }

    @Override
    public Map<String, String> execute() {
        Map<String, String> jvmThreadInfoMap = new LinkedHashMap<>();
        ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        bean.setThreadContentionMonitoringEnabled(true);
        ThreadInfo[] threads = bean.dumpAllThreads(dumpLocked, dumpLocked);
        putAttrToMap(jvmThreadInfoMap, type, "threadInfos", getJvmThreadInfos(threads));
        return jvmThreadInfoMap;
    }

    private String getJvmThreadInfos(ThreadInfo[] threads) {
        StringBuilder threadInfosBuilder = new StringBuilder();
        for (ThreadInfo threadInfo : threads) {
            putJvmThreadInfo(threadInfosBuilder, threadInfo);
        }
        return threadInfosBuilder.toString();
    }

    private void putJvmThreadInfo(StringBuilder threadInfosBuilder, ThreadInfo threadInfo) {
        threadInfosBuilder.append(toString(threadInfo));
    }

    private JvmThreadHeartBeatExecutor putAttrToMap(Map<String, String> m, String type, String subType, Object value) {
        m.put(String.format("%s%s%s", type, HeartBeatConstants.TYPE_DELIMIT, subType), String.valueOf(value));
        return this;
    }

    public String toString(ThreadInfo threadInfo) {
        StringBuilder sb = new StringBuilder("\"");
        sb.append(threadInfo.getThreadName());
        sb.append("\"");
        sb.append(" Id=");
        sb.append(threadInfo.getThreadId());
        sb.append(" ");
        sb.append(threadInfo.getThreadState());
        if (threadInfo.getLockName() != null) {
            sb.append(" on ");
            sb.append(threadInfo.getLockName());
        }
        if (threadInfo.getLockOwnerName() != null) {
            sb.append(" owned by \"");
            sb.append(threadInfo.getLockOwnerName());
            sb.append("\" Id=");
            sb.append(threadInfo.getLockOwnerId());
        }
        if (threadInfo.isSuspended()) {
            sb.append(" (suspended)");
        }
        if (threadInfo.isInNative()) {
            sb.append(" (in native)");
        }
        sb.append(" (BlockedTime=");
        sb.append(threadInfo.getBlockedTime());
        sb.append(", BlockedCount=");
        sb.append(threadInfo.getBlockedCount());
        sb.append(", WaitedTime=");
        sb.append(threadInfo.getWaitedTime());
        sb.append(", WaitedCount=");
        sb.append(threadInfo.getWaitedCount());
        sb.append(")");
        sb.append('\n');
        StackTraceElement[] stackTrace = threadInfo.getStackTrace();
        int i = 0;
        for (; i < stackTrace.length && i < MAX_FRAMES; i++) {
            StackTraceElement ste = stackTrace[i];
            sb.append("\tat ");
            sb.append(ste.toString());
            sb.append('\n');
            if (i == 0 && threadInfo.getLockInfo() != null) {
                Thread.State ts = threadInfo.getThreadState();
                switch (ts) {
                    case BLOCKED:
                        sb.append("\t-  blocked on ");
                        sb.append(threadInfo.getLockInfo());
                        sb.append('\n');
                        break;
                    case WAITING:
                        sb.append("\t-  waiting on ");
                        sb.append(threadInfo.getLockInfo());
                        sb.append('\n');
                        break;
                    case TIMED_WAITING:
                        sb.append("\t-  waiting on ");
                        sb.append(threadInfo.getLockInfo());
                        sb.append('\n');
                        break;
                    default:
                }
            }
            MonitorInfo[] lockedMonitors = threadInfo.getLockedMonitors();
            for (MonitorInfo mi : lockedMonitors) {
                if (mi.getLockedStackDepth() == i) {
                    sb.append("\t-  locked ");
                    sb.append(mi);
                    sb.append('\n');
                }
            }
        }
        if (i < stackTrace.length) {
            sb.append("\t...");
            sb.append('\n');
        }

        LockInfo[] locks = threadInfo.getLockedSynchronizers();
        if (locks.length > 0) {
            sb.append("\n\tNumber of locked synchronizers = ");
            sb.append(locks.length);
            sb.append('\n');
            for (LockInfo li : locks) {
                sb.append("\t- ");
                sb.append(li);
                sb.append('\n');
            }
        }
        sb.append('\n');
        return sb.toString();
    }
}

