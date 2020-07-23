package com.github.paganini2008.springworld.scheduler;

/**
 * 
 * SqlScripts
 *
 * @author Fred Feng
 * @since 1.0
 */
public abstract class SqlScripts {

	public static final String DEF_DDL_JOB_DETAIL = "create table my_job_detail(job_name varchar(255) unique not null, description varchar(255), job_class_name varchar(255) not null, attachment blob, create_date timestamp default current_timestamp)";
	public static final String DEF_DDL_JOB_PANEL = "create table my_job_panel(job_name varchar(255) unique not null, job_state varchar(45) not null, last_running_state varchar(45), completed_count bigint default 0, failed_count bigint default 0, skipped_count bigint default 0, last_execution_time timestamp, last_completion_time timestamp, next_execution_time timestamp)";
	public static final String DEF_DDL_JOB_CRON_TRIGGER = "create table my_job_cron_trigger(job_name varchar(255) unique not null,cron varchar(45) not null)";
	public static final String DEF_DDL_JOB_PERIODIC_TRIGGER = "create table my_job_periodic_trigger(job_name varchar(255) unique not null, period bigint, delay bigint, period_time_unit int, delay_time_unit int)";
	public static final String DEF_DDL_JOB_DEPENDENCY = "create table my_job_dependency(job_name varchar(255) not null, dependent_job_name varchar(255) not null)";

	public static final String DEF_INSERT_JOB_DETAIL = "insert into my_job_detail(job_name,job_class_name,description,attachment,create_date) values (?,?,?,?,?)";
	public static final String DEF_INSERT_JOB_PANEL = "insert into my_job_panel(job_name,job_state) values (?,?)";

	public static final String DEF_UPDATE_JOB_PANEL_WHEN_START = "update my_job_panel set job_state=?, last_execution_time=? where job_name=?";
	public static final String DEF_UPDATE_JOB_PANEL_WHEN_END = "update my_job_panel set job_state=?, last_running_state=?, completed_count+=?, failed_count+=?, skipped_count+=?, last_completion_time=?, next_execution_time=? where job_name=?";
	public static final String DEF_UPDATE_JOB_STATE = "update my_job_panel set job_state=? where job_name=?";

	public static final String DEF_SELECT_JOB_NAME_EXISTS = "select count(*) from my_job_detail where job_name=? and job_class_name=?";
	public static final String DEF_SELECT_ALL_JOB_DETAIL = "select * from my_job_detail";
	public static final String DEF_SELECT_JOB_DETAIL = "select * from my_job_detail where job_name=?";
	public static final String DEF_SELECT_JOB_PANEL_DETAIL = "select * from my_job_panel where job_name=?";
	public static final String DEF_DELETE_JOB_DETAIL = "delete from my_job_detail where job_name=?";

	public static final String DEF_INSERT_JOB_DEPENDENCY = "insert into my_job_dependency(job_name,dependent_job_name) values (?,?)";

}
