package com.lts.example.jobdispatcher;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Strings;
import com.lts.core.domain.Action;
import com.lts.core.domain.Job;
import com.lts.core.logger.Logger;
import com.lts.core.logger.LoggerFactory;
import com.lts.tasktracker.Result;
import com.lts.tasktracker.logger.BizLogger;
import com.lts.tasktracker.runner.JobRunner;
import com.lts.tasktracker.runner.LtsLoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by 28797575@qq.com hongliangpan on 2016/2/29.
 */

public class JobRunnerDispatcher implements JobRunner {
    protected static final Logger LOGGER = LoggerFactory.getLogger(JobRunnerDispatcher.class);
    private static final ConcurrentHashMap<String, JobRunner>
            JOB_RUNNER_MAP = new ConcurrentHashMap<String, JobRunner>();

    static {
        try {
            JobRunnerScanner.scan("com.glodon.ysg", JOB_RUNNER_MAP);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(),e);
        }
    }

    @Override
    public Result run(Job job) throws Throwable {
        String type = job.getParam("type");
        if(Strings.isNullOrEmpty(type)){
           return  new Result(Action.EXECUTE_FAILED, "没有类型参数.type is null");
        }
        JobRunner jobRunner= JOB_RUNNER_MAP.get(type);
        if(null==jobRunner){
            return  new Result(Action.EXECUTE_FAILED, "没有注册作业.type="+type);
        }
        Result result= jobRunner.run(job);
        BizLogger bizLogger = LtsLoggerFactory.getBizLogger();
        bizLogger.info("任务完成:"+ JSON.toJSONString(result));
        return result;
    }
    public void register(String type ,JobRunner runner){
        JOB_RUNNER_MAP.put(type, runner);
    }

}