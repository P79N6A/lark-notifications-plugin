package com.xyq.lark;

import hudson.Extension;
import hudson.model.*;
import hudson.model.listeners.RunListener;
import hudson.tasks.Publisher;
import hudson.triggers.SCMTrigger;

import javax.annotation.Nonnull;
import java.util.Map;


@Extension
public class JobListener extends RunListener<AbstractBuild> {


    public JobListener() {
        super(AbstractBuild.class);
    }

    @Override
    public void onStarted(AbstractBuild r, TaskListener listener) {

        getService(r, listener).start(getDescription(r));
    }

    @Override
    public void onCompleted(AbstractBuild r, @Nonnull TaskListener listener) {
        Result result = r.getResult();
        if (null != result && result.equals(Result.SUCCESS)) {
            getService(r, listener).success(getDescription(r));
        } else if (null != result && result.equals(Result.FAILURE)) {
            getService(r, listener).failed();
        } else {
            getService(r, listener).abort();
        }
    }

    private LarkService getService(AbstractBuild build, TaskListener listener) {
        Map<Descriptor<Publisher>, Publisher> map = build.getProject().getPublishersList().toMap();
        for (Publisher publisher : map.values()) {
            if (publisher instanceof LarkNotifier) {
                return ((LarkNotifier) publisher).newLarkService(build, listener);
            }
        }
        return null;
    }

    private String getDescription(AbstractBuild r){

        String description=null;
        CauseAction causeAction = r.getAction(CauseAction.class);
        if (causeAction!=null){
            Cause scmCause = causeAction.findCause(SCMTrigger.SCMTriggerCause.class);
            if (scmCause==null){
                description =  causeAction.getCauses().get(0).getShortDescription();
            }
        }
        return description;
    }


}
