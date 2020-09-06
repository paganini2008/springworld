package com.github.paganini2008.springworld.crontab;

/**
 * 
 * SqlScripts
 *
 * @author Fred Feng
 * @since 1.0
 */
public abstract class SqlScripts {

	public static final String DEF_DDL_CLUSTER_DETAIL = "create table crontab_server_detail(id int primary key auto_increment, cluster_name varchar(45) not null, group_name varchar(45) not null, instance_id varchar(255) not null, context_path varchar(255) not null, start_date timestamp not null, contact_person varchar(45), contact_email varchar(255))";
	public static final String DEF_DDL_JOB_DETAIL = "create table crontab_job_detail(job_id int primary key auto_increment, cluster_name varchar(45) not null, group_name varchar(45) not null, job_name varchar(255) not null, job_class_name varchar(255) not null, description varchar(255), attachment varchar(255), email varchar(45), retries int, create_date timestamp)";
	public static final String DEF_DDL_JOB_TRIGGER = "create table crontab_job_trigger(job_id int not null, trigger_type int not null, trigger_description text not null, start_date timestamp, end_date timestamp)";
	public static final String DEF_DDL_JOB_RUNTIME = "create table crontab_job_runtime(job_id int not null, job_state int not null, last_running_state int, last_execution_time timestamp, last_completion_time timestamp, next_execution_time timestamp)";
	public static final String DEF_DDL_JOB_TRACE = "create table crontab_job_trace(trace_id int primary key auto_increment, job_id int not null, running_state int, complete int, failed int, skipped int, execution_time timestamp, completion_time timestamp)";
	public static final String DEF_DDL_JOB_EXCEPTION = "create table crontab_job_exception(trace_id int not null, job_id int not null, exception_content varchar(600))";
	public static final String DEF_DDL_JOB_DEPENDENCY = "create table crontab_job_dependency(job_id int not null, dependent_job_id int not null)";

	public static final String DEF_INSERT_CLUSTER_DETAIL = "insert into crontab_server_detail(cluster_name,group_name,instance_id,context_path,start_date,contact_person,contact_email) values (?,?,?,?,?,?,?)";
	public static final String DEF_INSERT_JOB_DETAIL = "insert into crontab_job_detail(cluster_name,group_name,job_name,job_class_name,description,attachment,email,retries,create_date) values (?,?,?,?,?,?,?,?,?)";
	public static final String DEF_INSERT_JOB_RUNTIME = "insert into crontab_job_runtime(job_id, job_state) values (?,?)";
	public static final String DEF_INSERT_JOB_TRACE = "insert into crontab_job_trace(job_id, running_state, complete, failed, skipped, execution_time, completion_time) values (?,?,?,?,?,?,?)";
	public static final String DEF_INSERT_JOB_EXCEPTION = "insert into crontab_job_exception(trace_id, job_id, exception_content) values (?,?,?)";
	public static final String DEF_INSERT_JOB_TRIGGER = "insert into crontab_job_trigger(job_id, trigger_type, trigger_description, start_date, end_date) values (?,?,?,?,?)";
	public static final String DEF_INSERT_JOB_DEPENDENCY = "insert into crontab_job_dependency(job_id, dependent_job_id) value (?,?)";

	public static final String DEF_UPDATE_JOB_DETAIL = "update crontab_job_detail set description=?, attachment=?, email=?, retries=? where group_name=? and job_name=? and job_class_name=?";
	public static final String DEF_UPDATE_JOB_TRIGGER = "update crontab_job_trigger set trigger_type=?, trigger_description=?, start_date=?, end_date=? where job_id=?";
	public static final String DEF_UPDATE_JOB_RUNNING_BEGIN = "update crontab_job_runtime set job_state=?, last_execution_time=?, next_execution_time=? where job_id=(select job_id from crontab_job_detail where group_name=? and job_name=? and job_class_name=?)";
	public static final String DEF_UPDATE_JOB_RUNNING_END = "update crontab_job_runtime set job_state=?, last_running_state=?, last_completion_time=? where job_id=(select job_id from crontab_job_detail where group_name=? and job_name=? and job_class_name=?)";
	public static final String DEF_UPDATE_JOB_STATE = "update crontab_job_runtime set job_state=? where job_id=?";

	public static final String DEF_DELETE_JOB_DEPENDENCY = "delete from crontab_job_dependency where job_id=?";
	public static final String DEF_CLEAN_CLUSTER_DETAIL = "delete from crontab_server_detail";
	public static final String DEF_DELETE_CLUSTER_DETAIL = "delete from crontab_server_detail where cluster_name=?";

	public static final String DEF_SELECT_CLUSTER_DETAIL = "select * from crontab_server_detail where cluster_name=?";
	public static final String DEF_SELECT_CLUSTER_CONTEXT_PATH = "select distinct context_path from crontab_server_detail where cluster_name=?";
	public static final String DEF_SELECT_ALL_JOB_DETAIL = "select * from crontab_job_detail";
	public static final String DEF_SELECT_JOB_ID = "select job_id from crontab_job_detail where cluster_name=? and group_name=? and job_name=? and job_class_name=?";
	public static final String DEF_SELECT_ALL_JOB_TRIGGER_DEADLINE = "select c.cluster_name,c.group_name,c.job_name,c.job_class_name,a.* from crontab_job_trigger a join crontab_job_runtime b on a.job_id=b.job_id join crontab_job_detail c on c.job_id=b.job_id where a.end_date is not null and b.job_state<4";
	public static final String DEF_SELECT_JOB_KEYS = "select * from crontab_job_detail a where a.cluster_name=? and exists (select job_id from crontab_job_trigger where job_id=a.job_id and trigger_type=?)";
	public static final String DEF_SELECT_JOB_DETAIL_BY_GROUP_NAME = "select * from crontab_job_detail where group_name=?";
	public static final String DEF_SELECT_JOB_DETAIL_BY_OTHER_GROUP_NAME = "select * from crontab_job_detail where group_name!=?";
	public static final String DEF_SELECT_JOB_NAME_EXISTS = "select count(*) from crontab_job_detail where cluster_name=? and group_name=? and job_name=? and job_class_name=?";
	public static final String DEF_SELECT_JOB_TRIGGER = "select * from crontab_job_trigger where job_id=?";
	public static final String DEF_SELECT_LATEST_EXECUTION_TIME_OF_DEPENDENT_JOBS = "select min(next_execution_time) from crontab_job_runtime where next_execution_time is not null and job_id in (%s)";
	public static final String DEF_SELECT_JOB_RUNTIME = "select * from crontab_job_runtime where job_id=?";
	public static final String DEF_SELECT_JOB_STAT = "select job_id, sum(complete) as completeCount, sum(failed) as failedCount, sum(skipped) as skippedCount from crontab_job_trace where job_id=(select job_id from crontab_job_detail where group_name=? and job_name=? and job_class_name=?)";
	public static final String DEF_SELECT_JOB_DEPENDENCIES = "select * from crontab_job_detail a where job_id in (select dependent_job_id from crontab_job_dependency where job_id=?)";
	public static final String DEF_SELECT_JOB_HAS_RELATION = "select count(*) from crontab_job_dependency where dependent_job_id=?";

	public static final String DEF_SELECT_JOB_INFO = "select a.*,b.* from crontab_job_detail a join crontab_job_runtime b on a.job_id=b.job_id";
	public static final String DEF_SELECT_JOB_DETAIL = "select * from crontab_job_detail where group_name=? and job_name=? and job_class_name=?";
	public static final String DEF_SELECT_ALL_JOB_STAT = "select job_id, sum(complete) as completeCount, sum(failed) as failedCount, sum(skipped) as skippedCount {0} from crontab_job_trace group by job_id {1}";
}
