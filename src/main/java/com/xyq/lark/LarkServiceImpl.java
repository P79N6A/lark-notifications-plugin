package com.xyq.lark;

import com.alibaba.fastjson.JSONObject;
import hudson.ProxyConfiguration;
import hudson.model.AbstractBuild;
import hudson.model.TaskListener;
import jenkins.model.Jenkins;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.lang.StringUtils;
import org.apache.http.util.TextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Random;


public class LarkServiceImpl implements LarkService {

    private Logger logger = LoggerFactory.getLogger(LarkService.class);

    private String shortDescription;
    private String sendUrlList;

    private boolean onStart;

    private boolean onSuccess;

    private boolean onFailed;

    private boolean onAbort;

    private TaskListener listener;

    private AbstractBuild build;


    private static final String apiUrl = "http://10.94.92.192:3003/news";//测试

    private ArrayList<String> jobUrlList;//测试用的
    private ArrayList<String> buildUrlList;//测试用的


    private String[] urlList;


    public static final String ONSTART = "start";
    public static final String ONSUCCESS = "success";
    public static final String ONFAILED = "failed";
    public static final String ONABORT = "abort";
    public static final String TYPE = "job";//todo 先写死job


    private String api;

    public LarkServiceImpl(String sendUrlList, boolean onStart, boolean onSuccess, boolean onFailed, boolean onAbort, TaskListener listener, AbstractBuild build) {
        this.sendUrlList = sendUrlList;
        this.onStart = onStart;
        this.onSuccess = onSuccess;
        this.onFailed = onFailed;
        this.onAbort = onAbort;
        this.listener = listener;
        this.build = build;

        //  addJobBuildList();//todo 测试
        urlList = parseSendList(sendUrlList);

    }

    private void addJobBuildList() {
        jobUrlList = new ArrayList<>();
        buildUrlList = new ArrayList<>();

        jobUrlList.add("https://ee.byted.org/ci/job/bear/job/android/job/creation-daily-build/");
        buildUrlList.add("https://ee.byted.org/ci/job/bear/job/android/job/creation-daily-build/1114/");

        jobUrlList.add("https://ee.byted.org/ci/job/bear/job/android/job/creation-daily-build/");
        buildUrlList.add("https://ee.byted.org/ci/job/bear/job/android/job/creation-daily-build/1113/");

        jobUrlList.add("https://ee.byted.org/ci/job/bear/job/android/job/creation-master/");
        buildUrlList.add("https://ee.byted.org/ci/job/bear/job/android/job/creation-master/989/");

        jobUrlList.add("https://ee.byted.org/ci/job/bear/job/android/job/creation-master/");
        buildUrlList.add("https://ee.byted.org/ci/job/bear/job/android/job/creation-master/988/");

        jobUrlList.add("https://ee.byted.org/ci/job/bear/job/android/job/doc-branch-test-task/");
        buildUrlList.add("https://ee.byted.org/ci/job/bear/job/android/job/doc-branch-test-task/1/");

        jobUrlList.add("https://ee.byted.org/ci/job/bear/job/android/job/doc-sdk-build-task/");
        buildUrlList.add("https://ee.byted.org/ci/job/bear/job/android/job/doc-sdk-build-task/184/");

        jobUrlList.add("https://ee.byted.org/ci/job/bear/job/android/job/doc-sdk-build-task-bot/");
        buildUrlList.add("https://ee.byted.org/ci/job/bear/job/android/job/doc-sdk-build-task-bot/1050/");

        jobUrlList.add("https://ee.byted.org/ci/job/bear/job/android/job/doc-sdk-build-task/");
        buildUrlList.add("https://ee.byted.org/ci/job/bear/job/android/job/doc-sdk-build-task-bot/1049/");

        jobUrlList.add("https://ee.byted.org/ci/job/bear/job/android/job/docs-branch-build-task-bot/");
        buildUrlList.add("https://ee.byted.org/ci/job/bear/job/android/job/docs-branch-build-task-bot/160/");

        jobUrlList.add("https://ee.byted.org/ci/job/bear/job/android/job/docs-branch-build-task-bot/");
        buildUrlList.add("https://ee.byted.org/ci/job/bear/job/android/job/docs-branch-build-task-bot/159/");

        jobUrlList.add("https://ee.byted.org/ci/job/bear/job/user/");
        buildUrlList.add("https://ee.byted.org/ci/job/bear/job/user/226/");
    }

    @Override
    public void start(String userDescription) {
        shortDescription = userDescription;
        logger.info("description:" + userDescription);
        if (onStart) {
            logger.info("send link msg from " + listener.toString());
            sendMsg(ONSTART);
        }

    }

    private String[] parseSendList(String sendList) {
        String[] urlLis = null;
        try {
            urlLis = sendList.split(";");

        } catch (Exception e) {
            logger.error("parse list error", e);
        }
        return urlLis;
    }

    private String getBuildUrl() {
        String getRootUrl = getDefaultURL();
        if (getRootUrl.endsWith("/")) {
            return getRootUrl + build.getUrl();
        } else {
            return getRootUrl + "/" + build.getUrl();
        }
    }

    private String getJobUrl() {
        String getRootUrl = getDefaultURL();
        if (getRootUrl.endsWith("/")) {
            return getRootUrl + build.getProject().getUrl();
        } else {
            return getRootUrl + "/" + build.getProject().getUrl();
        }
    }

    public String getDefaultURL() {
        Jenkins instance = Jenkins.getInstance();
        assert instance != null;
        if (instance.getRootUrl() != null) {
            return instance.getRootUrl();
        } else {
            return "";
        }
    }

    @Override
    public void success(String userDescription) {
        shortDescription = userDescription;
        logger.info("description:" + userDescription);
        if (onSuccess) {
            logger.info("send link msg from " + listener.toString());

            sendMsg(ONSUCCESS);
        }
    }

    @Override
    public void failed() {
        if (onFailed) {
            logger.info("send link msg from " + listener.toString());
            sendMsg(ONFAILED);
        }
    }

    @Override
    public void abort() {

        if (onAbort) {
            logger.info("send link msg from " + listener.toString());
            sendMsg(ONABORT);
        }
    }


    private void sendMsg(String type) {
        //todo 测试用的 明天删
/*        Random ran = new Random();
        int x = ran.nextInt(11);

        logger.info("x:"+x);
        String testJobUrl = jobUrlList.get(x);
        String testBuild = buildUrlList.get(x);*/

        if (urlList == null) {
            return;
        }
        HttpClient client = getHttpClient();
        System.out.println(urlList.toString());
        JSONObject body = new JSONObject();
        body.put("action", type);
        body.put("type", TYPE);
        body.put("name", build.getProject().getDisplayName());//test
        body.put("order", build.getDisplayName());//#7
        body.put("buildurl", getBuildUrl());
        body.put("joburl", getJobUrl());
/*        body.put("buildurl", testBuild);
        body.put("joburl", testJobUrl);*/

        body.put("duration", build.getDuration());

        if ((StringUtils.equals(type, ONSTART) || StringUtils.equals(type, ONSUCCESS)) && shortDescription != null) {
            body.put("causeby", shortDescription);
        }
        // body.put("usename",userName);//todo 拿不到这个


        StringRequestEntity requestEntity = null;
        try {
            requestEntity = new StringRequestEntity(body.toJSONString(), "application/json", "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            logger.error("requestEntity encode error", e);
        }
        logger.info(body.toJSONString());
        for (String s : urlList) {

            if (!TextUtils.isEmpty(s)) {

                PostMethod post = new PostMethod(s);

                post.setRequestEntity(requestEntity);

                try {
                    client.executeMethod(post);
                    logger.info(post.getResponseBodyAsString());
                } catch (IOException e) {
                    logger.error("send msg error", e);
                } finally {
                    post.releaseConnection();
                }
            }
        }

    }


    private HttpClient getHttpClient() {
        HttpClient client = new HttpClient();
        Jenkins jenkins = Jenkins.getInstance();
        if (jenkins != null && jenkins.proxy != null) {
            ProxyConfiguration proxy = jenkins.proxy;
            if (proxy != null && client.getHostConfiguration() != null) {
                client.getHostConfiguration().setProxy(proxy.name, proxy.port);
                String username = proxy.getUserName();
                String password = proxy.getPassword();
                if (username != null && !"".equals(username.trim())) {
                    logger.info("Using proxy authentication (user=" + username + ")");
                    client.getState().setProxyCredentials(AuthScope.ANY,
                            new UsernamePasswordCredentials(username, password));
                }
            }
        }
        return client;
    }
}
