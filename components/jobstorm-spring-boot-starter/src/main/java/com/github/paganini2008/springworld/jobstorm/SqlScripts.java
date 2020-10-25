package com.github.paganini2008.springworld.jobstorm;

/**
 * 
 * SqlScripts
 *
 * @author Fred Feng
 * @since 1.0
 */
public abstract class SqlScripts {

	public static final String DEF_DDL_CLUSTER_DETAIL = "create table js_server_detail(id int primary key auto_increment, cluster_name varchar(45) not null, group_name varchar(45) not null, instance_id varchar(255) not null, context_path varchar(255) not null, start_date timestamp not null, contact_person varchar(45), contact_email varchar(255))";
	public static final String DEF_DDL_JOB_DETAIL = "create table js_job_detail(job_id int primary key auto_increment, cluster_name varchar(45) not null, group_name varchar(45) not null, job_name varchar(255) not null, job_class_name varchar(255) not null, description varchar(255), attachment varchar(255), email varchar(45), retries int, weight int, timeout bigint, create_date timestamp)";
	public static final String DEF_DDL_JOB_TRIGGER = "create table js_job_trigger(job_id int not null, trigger_type int not null, trigger_description text not null, start_date timestamp, end_date timestamp, repeat_count int)";
	public static final String DEF_DDL_JOB_RUNTIME = "create table js_job_runtime(job_id int not null, job_state int not null, last_running_state int, last_execution_time timestamp, last_completion_time timestamp, next_execution_time timestamp)";
	public static final String DEF_DDL_JOB_TRACE = "create table js_job_trace(trace_id bigint primary key, job_id int not null, running_state int, address varchar(45), instance_id varchar(45), completed int, failed int, skipped int, finished int, retries int, execution_time timestamp, completion_time timestamp)";
	public static final String DEF_DDL_JOB_EXCEPTION = "create table js_job_exception(trace_id bigint not null, job_id int not null, stajs_trace varchar(600))";
	public static final String DEF_DDL_JOB_LOG = "create table js_job_log(trace_id bigint not null, job_id int not null, level varchar(45), log varchar(600), create_date timestamp)";
	public static final String DEF_DDL_JOB_DEPENDENCY = "create table js_job_dependency(job_id int not null, dependent_job_id int not null, dependency_type int not null)";

	public static final String DEF_INSERT_CLUSTER_DETAIL = "insert into js_server_detail(cluster_name,group_name,instance_id,context_path,start_date,contact_person,contact_email) values (?,?,?,?,?,?,?)";
	public static final String DEF_INSERT_JOB_DETAIL = "insert into js_job_detail(cluster_name,group_name,job_name,job_class_name,description,attachment,email,retries,weight,timeout,create_date) values (?,?,?,?,?,?,?,?,?,?,?)";
	public static final String DEF_INSERT_JOB_RUNTIME = "insert into js_job_runtime(job_id, job_state) values (?,?)";
	public static final String DEF_INSERT_JOB_TRACE = "insert into js_job_trace(trace_id, job_id, running_state, address, instance_id, completed, failed, skipped, finished, retries, execution_time, completion_time) values (?,?,?,?,?,?,?,?,?,?,?,?)";
	public static final String DEF_INSERT_JOB_EXCEPTION = "insert into js_job_exception(trace_id, job_id, stajs_trace) values (?,?,?)";
	public static final String DEF_INSERT_JOB_LOG = "insert into js_job_log(trace_id, job_id, level, log, create_date) values (?,?,?,?,?)";
	public static final String DEF_INSERT_JOB_TRIGGER = "insert into js_job_trigger(job_id, trigger_type, trigger_description, start_date, end_date, repeat_count) values (?,?,?,?,?,?)";
	public static final String DEF_INSERT_JOB_DEPENDENCY = "insert into js_job_dependency(job_id, dependent_job_id, dependency_type) value (?,?,?)";

	public static final String DEF_UPDATE_JOB_DETAIL = "update js_job_detail set description=?, attachment=?, email=?, retries=?, weight=?, timeout=? where cluster_name=? and group_name=? and job_name=? and job_class_name=?";
	public static final String DEF_UPDATE_JOB_TRIGGER = "update js_job_trigger set trigger_type=?, trigger_description=?, start_date=?, end_date=?, repeat_count=? where job_id=?";
	public static final String DEF_UPDATE_JOB_RUNNING_BEGIN = "update js_job_runtime set job_state=?, last_execution_time=?, next_execution_time=? where job_id=(select job_id from js_job_detail where cluster_name=? and group_name=? and job_name=? and job_class_name=?)";
	public static final String DEF_UPDATE_JOB_RUNNING_END = "update js_job_runtime set job_state=?, last_running_state=?, last_completion_time=? where job_id=(select job_id from js_job_detail where cluster_name=? and group_name=? and job_name=? and job_class_name=?)";
	public static final String DEF_UPDATE_JOB_STATE = "update js_job_runtime set job_state=? where job_id=?";

	public static final String DEF_DELETE_JOB_DEPENDENCY = "delete from js_job_dependency where job_id=? and dependency_type=?";
	public static final String DEF_CLEAN_CLUSTER_DETAIL = "delete from js_server_detail";
	public static final String DEF_DELETE_CLUSTER_DETAIL = "delete from js_server_detail where cluster_name=?";

	public static final String DEF_SELECT_CLUSTER_NAME = "select distinct cluster_name from js_server_detail";
	public static final String DEF_SELECT_CLUSTER_DETAIL = "select * from js_server_detail where cluster_name=?";
	public static final String DEF_SELECT_CLUSTER_CONTEXT_PATH = "select distinct context_path from js_server_detail where cluster_name=?";
	public static final String DEF_SELECT_ALL_JOB_DETAIL = "select * from js_job_detail";
	public static final String DEF_SELECT_ALL_AVAILABLE_JOB_DETAIL = "select a.* from js_job_detail a join js_job_runtime b on a.job_id=b.job_id where b.job_state<4";
	public static final String DEF_SELECT_JOB_ID = "select job_id from js_job_detail where cluster_name=? and group_name=? and job_name=? and job_class_name=?";
	public static final String DEF_SELECT_ALL_JOB_TRIGGER_DEADLINE = "select c.cluster_name,c.group_name,c.job_name,c.job_class_name,a.* from js_job_trigger a join js_job_runtime b on a.job_id=b.job_id join js_job_detail c on c.job_id=b.job_id where a.end_date is not null and b.job_state<4";
	public static final String DEF_SELECT_JOB_KEYS = "select * from js_job_detail a where a.cluster_name=? and exists (select job_id from js_job_trigger where job_id=a.job_id and trigger_type=?)";
	public static final String DEF_SELECT_JOB_DETAIL_BY_GROUP_NAME = "select a.* from js_job_detail a join js_job_runtime b on a.job_id=b.job_id where a.group_name=? and b.job_state<4";
	public static final String DEF_SELECT_JOB_DETAIL_BY_OTHER_GROUP_NAME = "select a.* from js_job_detail a join js_job_runtime b on a.job_id=b.job_id where a.group_name!=? and b.job_state<4";
	public static final String DEF_SELECT_JOB_NAME_EXISTS = "select count(*) from js_job_detail where cluster_name=? and group_name=? and job_name=? and job_class_name=?";
	public static final String DEF_SELECT_JOB_TRIGGER = "select * from js_job_trigger where job_id=?";
	public static final String DEF_SELECT_LATEST_EXECUTION_TIME_OF_DEPENDENT_JOBS = "select min(next_execution_time) from js_job_runtime where next_execution_time is not null and job_id in (%s)";
	public static final String DEF_SELECT_JOB_RUNTIME = "select * from js_job_runtime where job_id=?";
	public static final String DEF_SELECT_JOB_RUNTIME_BY_JOB_STATE = "select a.*,b.* from js_job_detail a join js_job_runtime b on a.job_id=b.job_id where b.job_state=?";

	public static final String DEF_SELECT_JOB_STAT = "select job_id, sum(complete) as completeCount, sum(failed) as failedCount, sum(skipped) as skippedCount from js_job_trace where job_id=(select job_id from js_job_detail where group_name=? and job_name=? and job_class_name=?)";
	public static final String DEF_SELECT_JOB_DEPENDENCIES = "select * from js_job_detail a where job_id in (select dependent_job_id from js_job_dependency where job_id=? and dependency_type=?)";
	public static final String DEF_SELECT_JOB_HAS_RELATION = "select count(*) from js_job_dependency where dependent_job_id=? and dependency_type=?";
	public static final String DEF_SELECT_JOB_RELATIONS = "select * from js_job_detail where job_id in (select job_id from js_job_dependency where dependent_job_id=? and dependency_type=?)";

	public static final String DEF_SELECT_JOB_TRACE = "select * from js_job_trace where job_id=? and execution_time between ? and ?";
	public static final String DEF_SELECT_JOB_INFO = "select a.*,b.job_state,b.last_running_state,b.last_execution_time,b.last_completion_time,b.next_execution_time,c.trigger_type,c.trigger_description,c.start_date,c.end_date from js_job_detail a join js_job_runtime b on b.job_id=a.job_id join js_job_trigger c on c.job_id=b.job_id where a.cluster_name=? order by create_date desc";
	public static final String DEF_SELECT_JOB_DETAIL = "select * from js_job_detail where cluster_name=? and group_name=? and job_name=? and job_class_name=?";
	public static final String DEF_SELECT_JOB_LOG = "select * from js_job_log where job_id=? and trace_id=?";
	public static final String DEF_SELECT_JOB_EXCEPTION = "select * from js_job_exception where job_id=? and trace_id=?";
	public static final String DEF_SELECT_ALL_JOB_STAT = "select job_id, sum(complete) as completeCount, sum(failed) as failedCount, sum(skipped) as skippedCount {0} from js_job_trace group by job_id {1}";
}
