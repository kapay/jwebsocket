/* This script creates the uses jwsDemo and grants him the "demo" rights 
 * This database user is supposed to be used for external user access for usual work purposes!
 */

/* use this database schema */
use `jwebsocket`;

/*  demo tables */
grant select,insert,delete,update on `jwebsocket`.`demo_master` to `jwsDemo`@`localhost`;
grant select,insert,delete,update on `jwebsocket`.`demo_child` to `jwsDemo`@`localhost`;
grant select,insert,delete,update on `jwebsocket`.`demo_lookup` to `jwsDemo`@`localhost`;

/* necessary access to stored procedures */
grant execute on procedure `jwebsocket`.`getSequence` to `jwsDemo`@`localhost`;
