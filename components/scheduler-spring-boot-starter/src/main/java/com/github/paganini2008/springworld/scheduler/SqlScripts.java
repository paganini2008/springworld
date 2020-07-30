package com.github.paganini2008.springworld.scheduler;

/**
 * 
 * SqlScripts
 *
 * @author Fred Feng
 * @since 1.0
 */
public abstract class SqlScripts {

	public static final String DEF_DDL_JOB_DETAIL = "create table cluster_job_detail(job_id int primary key auto_increment, job_name varchar(255) not null, job_class_name varchar(255) not null, group_name varchar(45) not null, job_type int, description varchar(3000), attachment varchar(255), create_date timestamp)";
	public static final String DEF_DDL_JOB_TRIGGER = "create table cluster_job_trigger(job_id int not null, json varchar(255) not null)";
	public static final String DEF_DDL_JOB_RUNTIME = "create table cluster_job_runtime(job_id int not null, job_state int not null, last_running_state int, last_execution_time timestamp, last_completion_time timestamp, next_execution_time timestamp)";
	public static final String DEF_DDL_JOB_TRACE = "create table cluster_job_trace(trace_id int primary key auto_increment, job_id int not null, running_state int, complete int, failed int, skipped int, execution_time timestamp, completion_time timestamp)";
	public static final String DEF_DDL_JOB_EXCEPTION = "create table cluster_job_exception(trace_id int not null, job_id int not null, exception_content varchar(900))";

	public static final String DEF_INSERT_JOB_DETAIL = "insert into cluster_job_detail(job_name,job_class_name,group_name,job_type,description,attachment,create_date) values (?,?,?,?,?,?,?)";
	public static final String DEF_INSERT_JOB_RUNTIME = "insert into cluster_job_runtime(job_id, job_state) values (?,?)";
	public static final String DEF_INSERT_JOB_TRACE = "insert into cluster_job_trace(job_id, running_state, complete, failed, skipped, execution_time, completion_time) values (?,?,?,?,?,?,?)";
	public static final String DEF_INSERT_JOB_EXCEPTION = "insert into cluster_job_exception(trace_id, job_id, exception_content) values (?,?,?)";

	public static final String DEF_UPDATE_JOB_RUNTIME_START = "update cluster_job_runtime set job_state=?, last_execution_time=? where job_id=(select id from cluster_job_detail where job_name=? and job_class_name=?)";
	public static final String DEF_UPDATE_JOB_RUNTIME_END = "update cluster_job_runtime set job_state=?, last_running_state=?, last_completion_time=?, next_execution_time=? where job_id=(select job_id from cluster_job_detail where job_name=? and job_class_name=?)";
	public static final String DEF_UPDATE_JOB_RUNTIME = "update cluster_job_runtime set job_state=? where job_name=? and job_class_name=?";

	public static final String DEF_SELECT_ALL_JOB_DETAIL = "select * from cluster_job_detail";
	public static final String DEF_SELECT_JOB_NAME_EXISTS = "select count(*) from cluster_job_detail where job_name=? and job_class_name=?";
	public static final String DEF_SELECT_JOB_TRIGGER = "select * from cluster_job_trigger where job_id=?";

	public static final String DEF_SELECT_JOB_RUNTIME = "select * from cluster_job_runtime where job_id=(select job_id from cluster_job_detail where job_name=? and job_class_name=?)";
	public static final String DEF_SELECT_JOB_STAT = "select job_id, sum(complete) as completeCount, sum(failed) as failedCount, sum(skipped) as skippedCount from cluster_job_trace where job_id=(select job_id from cluster_job_detail where job_name=? and job_class_name=?)";

	public static final String DEF_SELECT_ALL_JOB_INFO = "select a.*,b.* from cluster_job_detail a join cluster_job_runtime b on a.job_id=b.job_id";
	public static final String DEF_SELECT_JOB_DETAIL = "select * from cluster_job_detail where job_name=? and job_class_name=?";
	public static final String DEF_SELECT_ALL_JOB_STAT = "select job_id, sum(complete) as completeCount, sum(failed) as failedCount, sum(skipped) as skippedCount {0} from cluster_job_trace group by job_id {1}";
	public static final String DEF_DELETE_JOB_DETAIL = "delete from cluster_job_detail where job_name=? and job_class_name=?";
}
