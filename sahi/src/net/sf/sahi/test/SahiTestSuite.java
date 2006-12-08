/**
 * Copyright  2006  V Narayan Raman
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

package net.sf.sahi.test;

import net.sf.sahi.issue.IssueCreator;
import net.sf.sahi.issue.IssueReporter;
import net.sf.sahi.report.SahiReporter;
import net.sf.sahi.session.Session;
import net.sf.sahi.session.Status;
import net.sf.sahi.util.Utils;

import java.io.File;
import java.util.*;

public class SahiTestSuite {
    private final String suitePath;
    private final String base;
    private List tests = new ArrayList();
    private Map testsMap = new HashMap();
    private int currentTestIndex = 0;
    private final String sessionId;
    private final String browser;
    private String suiteName;
    private int finishedTests = 0;
    private List listReporter = new ArrayList();
    private IssueReporter issueReporter;
    private String browserOption;
    private int availableThreads = 0;
    private static HashMap suites = new HashMap();

    public SahiTestSuite(String suitePath, String base, String browser, String sessionId, String browseroption) {
        this.suitePath = suitePath;
        this.base = base;
        this.browser = browser;
        this.sessionId = Utils.stripChildSessionId(sessionId);
        this.browserOption = browseroption;

        setSuiteName();
        loadScripts();
        suites.put(this.sessionId, this);
    }

    private void loadScripts() {
        this.tests = new SuiteLoader(suitePath, base).loadScripts();
        for (Iterator iterator = tests.iterator(); iterator.hasNext();) {
            TestLauncher script = (TestLauncher) iterator.next();
            script.setSessionId(sessionId);
            script.setBrowser(browser);
            script.setBrowserOption(browserOption);
            testsMap.put(new File(script.getScriptName()).getName(), script);
        }
    }

    public List getListReporter() {
        return listReporter;
    }

    public static SahiTestSuite getSuite(String sessionId) {
        return (SahiTestSuite) suites.get(Utils.stripChildSessionId(sessionId));
    }

    private void executeTest() {
        TestLauncher test = (TestLauncher) tests.get(currentTestIndex);
        test.execute();
        currentTestIndex++;
    }

    public synchronized void notifyComplete(String scriptName) {
        ((TestLauncher) (testsMap.get(scriptName))).stop();
        finishedTests++;
        availableThreads++;
        this.notify();
    }

    private void setSuiteName() {
        this.suiteName = suitePath;
        int lastIndexOfSlash = suitePath.lastIndexOf("/");
        if (lastIndexOfSlash != -1) {
            this.suiteName = suitePath.substring(lastIndexOfSlash + 1);
        }
    }

    private void markSuiteStatus() {
        Status status = Status.SUCCESS;
        Session session;
        for (Iterator iterator = tests.iterator(); iterator.hasNext();) {
            TestLauncher testLauncher = (TestLauncher) iterator.next();
            session = Session.getInstance(testLauncher.getChildSessionId());
            if (Status.FAILURE.equals(session.getStatus())) {
                status = Status.FAILURE;
                break;
            }
        }
        session = Session.getInstance(this.sessionId);
        session.setStatus(status);
    }

    public void run() {
        Session session = Session.getInstance(this.sessionId);
        session.setStatus(Status.RUNNING);
        executeSuite();
        waitForSuiteCompletion();
        markSuiteStatus();
        generateSuiteReport();
        createIssues();
    }

    private void createIssues() {
        Session session = Session.getInstance(sessionId);
        if (Status.FAILURE.equals(session.getStatus()) && issueReporter != null) {
            issueReporter.reportIssue(tests);
        }
    }

    private synchronized void executeSuite() {
        while (currentTestIndex < tests.size()) {
            for (; availableThreads > 0 && currentTestIndex < tests.size(); availableThreads--) {
                this.executeTest();
            }
            try {
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void waitForSuiteCompletion() {
        while (finishedTests < tests.size()) {
            synchronized (this) {
                try {
                    this.wait(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void generateSuiteReport() {
        for (Iterator iterator = listReporter.iterator(); iterator.hasNext();) {
            SahiReporter reporter = (SahiReporter) iterator.next();
            reporter.generateSuiteReport(tests);
        }
    }

    public void setAvailableThreads(int availableThreads) {
        this.availableThreads = availableThreads;
    }

    public void addReporter(SahiReporter reporter) {
        reporter.setSuiteName(suiteName);
        listReporter.add(reporter);
    }

    public void addIssueCreator(IssueCreator issueCreator) {
        if (issueReporter == null) {
            issueReporter = new IssueReporter(suiteName);
        }
        issueReporter.addIssueCreator(issueCreator);
    }
}
