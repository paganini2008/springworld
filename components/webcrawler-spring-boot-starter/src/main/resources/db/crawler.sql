create table crawler_catalog(
	id bigint not null,
	name character varying(45) not null,
	url character varying(255) not null,
	path_pattern character varying(600),
	excluded_path_pattern character varying(600),
	type character varying(45) not null,
	last_modified timestamp without time zone
)

create table crawler_catalog_index(
	id bigint not null,
	catalog_id bigint not null,
	last_modified timestamp without time zone,
	version integer not null
)

create table crawler_resource(
	id bigint not null,
	title character varying(45) not null,
	html text,
	url character varying(600),
	type character varying(45) not null,
	last_modified timestamp without time zone,
	version integer not null,
	catalog_id bigint not null
)
