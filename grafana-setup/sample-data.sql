--
-- PostgreSQL database dump
--

\restrict wzZXzC8hKCpW1iStji1gJ4aWpDK2tTbAqoeVTgbSFJlAs8AEagqWp7WSxKF9uZe

-- Dumped from database version 14.20 (Debian 14.20-1.pgdg13+1)
-- Dumped by pg_dump version 14.20 (Debian 14.20-1.pgdg13+1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Data for Name: ground_truth; Type: TABLE DATA; Schema: public; Owner: quarkus
--

INSERT INTO public.ground_truth VALUES (99, 'cpio-2.15-1.el10', 25, 'Error: CPPCHECK_WARNING (CWE-562):
cpio-2.15/gnu/mktime.c:262: error[returnDanglingLifetime]: Returning pointer to local variable ''x'' that will be invalid when returning.
#  260|   {
#  261|     __time64_t x = t;
#  262|->   return convert (&x, tm);
#  263|   }
#  264|', true, 'Non-Issue', 'it calls gmtime_r on the value pointed by x', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.051962');
INSERT INTO public.ground_truth VALUES (287, 'libuser-0.64-7.el10', 8, 'Error: UNINIT (CWE-457):
libuser-0.64/lib/getdate.c:1068: var_decl: Declaring variable "yylval" without initializer.
libuser-0.64/lib/getdate.c:1893: uninit_use: Using uninitialized value "yylval".
# 1891|   
# 1892|     YY_IGNORE_MAYBE_UNINITIALIZED_BEGIN
# 1893|->   *++yyvsp = yylval;
# 1894|     YY_IGNORE_MAYBE_UNINITIALIZED_END
# 1895|', true, 'Non-Issue', 'Bison generated code. LGTM', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.14124');
INSERT INTO public.ground_truth VALUES (463, 'texinfo-7.1-2.el10', 18, '', false, 'Issue', NULL, NULL, '2025-11-18 16:18:56.203498');
INSERT INTO public.ground_truth VALUES (557, 'trace-cmd-3.2-2.el10', 69, 'Error: RESOURCE_LEAK (CWE-772):
trace-cmd-v3.2/lib/trace-cmd/trace-output.c:884: alloc_fn: Storage is returned from allocation function "strdup".
trace-cmd-v3.2/lib/trace-cmd/trace-output.c:884: var_assign: Assigning: "system" = storage returned from "strdup(file + events_len + 1)".
trace-cmd-v3.2/lib/trace-cmd/trace-output.c:885: identity_transfer: Passing "system" as argument 1 to function "strtok_r", which sets "ptr" to an offset off that argument.
trace-cmd-v3.2/lib/trace-cmd/trace-output.c:908: leaked_storage: Variable "ptr" going out of scope leaks the storage it points to.
#  906|   	}
#  907|   	globfree(&globbuf);
#  908|-> }
#  909|   
#  910|   static void', true, 'Non-Issue', 'The code make sure that the duplicated string doesn''t include the starting ''/'' which is also the delimiter, so the first token points a the same address as system before the call.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.218934');
INSERT INTO public.ground_truth VALUES (485, 'texinfo-7.1-2.el10', 40, 'Error: USE_AFTER_FREE (CWE-416):
texinfo-7.1/info/info.c:1048: freed_arg: "add_initial_nodes" frees "error".
texinfo-7.1/info/info.c:1069: pass_freed_arg: Passing freed pointer "error" as an argument to "info_error".
# 1067|         if (error)
# 1068|           {
# 1069|->           info_error ("%s", error);
# 1070|             exit (1);
# 1071|           }', true, NULL, 'add_initial_nodes() either frees "error" on line 427 but allocates it right again with xasprintf (wrapper to asprintf) on the following line 428, or frees "error" and sets it to NULL on lines 525, 539, 570', NULL, '2025-11-18 16:18:56.203498');
INSERT INTO public.ground_truth VALUES (486, 'texinfo-7.1-2.el10', 41, 'Error: CPPCHECK_WARNING (CWE-404):
texinfo-7.1/install-info/install-info.c:760: error[resourceLeak]: Resource leak: f
#  758|           }
#  759|         errno = 0;
#  760|->       return 0; /* unknown error */
#  761|       }
#  762|', false, NULL, 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-43595

', NULL, '2025-11-18 16:18:56.203498');
INSERT INTO public.ground_truth VALUES (484, 'texinfo-7.1-2.el10', 39, 'Error: VARARGS (CWE-237):
texinfo-7.1/info/util.c:38: va_init: Initializing va_list "v".
texinfo-7.1/info/util.c:39: missing_va_end: "va_end" was not called for "v".
#   37|     va_list v;
#   38|     va_start (v, template);
#   39|->   return xvasprintf (ptr, template, v);
#   40|   }
#   41|', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-43595

', NULL, '2025-11-18 16:18:56.203498');
INSERT INTO public.ground_truth VALUES (487, 'tpm2-tools-5.6-2.el10', 1, 'Error: OVERRUN (CWE-119):
tpm2-tools-5.6/lib/tpm2_alg_util.c:169: alias: Assigning: "scheme" = ""null"". "scheme" now points to byte 0 of ""null"" (which consists of 5 bytes).
tpm2-tools-5.6/lib/tpm2_alg_util.c:205: ptr_incr: Incrementing "scheme" by 5. "scheme" now points to byte 5 of ""null"" (which consists of 5 bytes).
tpm2-tools-5.6/lib/tpm2_alg_util.c:211: overrun-local: Overrunning array of 5 bytes at byte offset 5 by dereferencing pointer "scheme + 0".
#  209|                * commit-id.
#  210|                */
#  211|->             if (scheme[0] == ''\0'') {
#  212|                   scheme = "0";
#  213|               }', true, 'Non-Issue', 'Line 211 is guarded by "!strncmp(scheme, "ecdaa", 5)", ensuring that the variable scheme starts with "ecdaa". This also means that the variable consists of at least 6 bytes and accessing "(scheme+5)[0]" is still in bounds (well, as long as it''s a proper C string). Also, if the scheme is "null", the function should return already on line 182.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.214634');
INSERT INTO public.ground_truth VALUES (488, 'tpm2-tools-5.6-2.el10', 2, 'Error: RESOURCE_LEAK (CWE-772):
tpm2-tools-5.6/tools/tpm2_getekcertificate.c:546: alloc_fn: Storage is returned from allocation function "encode_ek_public".
tpm2-tools-5.6/tools/tpm2_getekcertificate.c:546: var_assign: Assigning: "ek_uri" = storage returned from "encode_ek_public()".
tpm2-tools-5.6/tools/tpm2_getekcertificate.c:566: leaked_storage: Variable "ek_uri" going out of scope leaks the storage it points to.
#  564|       free(ek_uri);
#  565|   out:
#  566|->     return ret;
#  567|   }
#  568|', false, 'Non-Issue', 'RHEL-23199', 'Memory leak claim at line 566 is disproven by explicit `free(ek_uri)` at line 564, which is guaranteed to execute before `return ret;` due to the `goto out;` statements, ensuring all paths deallocate `ek_uri`''s memory.', '2025-11-18 16:18:56.214634');
INSERT INTO public.ground_truth VALUES (489, 'trace-cmd-3.2-2.el10', 1, 'Error: INTEGER_OVERFLOW (CWE-190):
trace-cmd-v3.2/tracecmd/trace-record.c:2169: tainted_data_return: Called function "read(fd, stbuf, 8192UL)", and a possible return value may be less than zero.
trace-cmd-v3.2/tracecmd/trace-record.c:2169: cast_overflow: An assign that casts to a different type, which might trigger an overflow.
trace-cmd-v3.2/tracecmd/trace-record.c:2172: overflow: The expression "size + r" is considered to have possibly overflowed.
trace-cmd-v3.2/tracecmd/trace-record.c:2172: overflow: The expression "size + r + 1" is deemed overflowed because at least one of its arguments has overflowed.
trace-cmd-v3.2/tracecmd/trace-record.c:2172: overflow_sink: "size + r + 1", which might have underflowed, is passed to "realloc(buf, size + r + 1)".
# 2170|                   if (r <= 0)
# 2171|                           continue;
# 2172|->                 nbuf = realloc(buf, size+r+1);
# 2173|                   if (!nbuf) {
# 2174|                           free(buf);', true, 'Non-Issue', 'The code checks that read returns a positive value. We''re assured that 0 <= r < BUFSIZ', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.218934');
INSERT INTO public.ground_truth VALUES (490, 'trace-cmd-3.2-2.el10', 2, 'Error: BUFFER_SIZE (CWE-170):
trace-cmd-v3.2/lib/trace-cmd/trace-msg.c:1424: buffer_size_warning: Calling "strncpy" with a maximum size argument of 16 bytes on destination array "msg.tsync.sync_protocol_name" of size 16 bytes might leave the destination string unterminated.
# 1422|   
# 1423|   	tracecmd_msg_init(MSG_TIME_SYNC, &msg);
# 1424|-> 	strncpy(msg.tsync.sync_protocol_name, sync_protocol, TRACECMD_TSYNC_PNAME_LENGTH);
# 1425|   	msg.tsync.sync_msg_id = htonl(sync_msg_id);
# 1426|   	msg.hdr.size = htonl(ntohl(msg.hdr.size) + payload_size);', true, 'Non-Issue', 'sync_protocol is always a constant string of less than 15 characters: TRACECMD_TSYNC_PROTO_NONE, PTP_NAME or KVM_NAME. It''s all part of libtracecmd code and can''t come from anywhare else.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.218934');
INSERT INTO public.ground_truth VALUES (491, 'trace-cmd-3.2-2.el10', 3, 'Error: RESOURCE_LEAK (CWE-772):
trace-cmd-v3.2/tracecmd/trace-listen.c:698: open_fn: Returning handle opened by "create_client_file".
trace-cmd-v3.2/tracecmd/trace-listen.c:698: var_assign: Assigning: "ofd" = handle returned from "create_client_file(node, port)".
trace-cmd-v3.2/tracecmd/trace-listen.c:702: leaked_handle: Handle variable "ofd" going out of scope leaks the handle.
#  700|           pid_array = create_all_readers(node, port, pagesize, msg_handle);
#  701|           if (!pid_array)
#  702|->                 return -ENOMEM;
#  703|   
#  704|           /* on signal stop this msg */', true, 'Non-Issue', 'When process client exits, the program exits right away.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.218934');
INSERT INTO public.ground_truth VALUES (492, 'trace-cmd-3.2-2.el10', 4, 'Error: RESOURCE_LEAK (CWE-772):
trace-cmd-v3.2/lib/trace-cmd/trace-msg.c:1269: alloc_fn: Storage is returned from allocation function "calloc".
trace-cmd-v3.2/lib/trace-cmd/trace-msg.c:1269: var_assign: Assigning: "vagrs" = storage returned from "calloc(length, 1UL)".
trace-cmd-v3.2/lib/trace-cmd/trace-msg.c:1275: noescape: Resource "vagrs" is not freed or pointed-to in "memcpy". [Note: The source code implementation of the function has been overridden by a builtin model.]
trace-cmd-v3.2/lib/trace-cmd/trace-msg.c:1276: var_assign: Assigning: "buf_end" = "vagrs".
trace-cmd-v3.2/lib/trace-cmd/trace-msg.c:1277: var_assign: Assigning: "p" = "vagrs".
trace-cmd-v3.2/lib/trace-cmd/trace-msg.c:1288: leaked_storage: Variable "vagrs" going out of scope leaks the storage it points to.
trace-cmd-v3.2/lib/trace-cmd/trace-msg.c:1288: leaked_storage: Variable "buf_end" going out of scope leaks the storage it points to.
trace-cmd-v3.2/lib/trace-cmd/trace-msg.c:1288: leaked_storage: Variable "p" going out of scope leaks the storage it points to.
# 1286|   	*argc = nr_args;
# 1287|   	*argv = args;
# 1288|-> 	return 0;
# 1289|   
# 1290|   out:', false, 'Non-Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-40111

', 'Variables ''vagrs'', ''buf_end'', and ''p'' are not leaked as ''vagrs'' is explicitly freed at the ''out'' label (line 1292), which is reached upon return (line 1288), effectively releasing the entire allocated block.', '2025-11-18 16:18:56.218934');
INSERT INTO public.ground_truth VALUES (493, 'trace-cmd-3.2-2.el10', 5, 'Error: RESOURCE_LEAK (CWE-772):
trace-cmd-v3.2/lib/trace-cmd/trace-output.c:639: alloc_fn: Storage is returned from allocation function "get_tracing_file".
trace-cmd-v3.2/lib/trace-cmd/trace-output.c:639: var_assign: Assigning: "path" = storage returned from "get_tracing_file(handle, "events/header_page")".
trace-cmd-v3.2/lib/trace-cmd/trace-output.c:648: leaked_storage: Variable "path" going out of scope leaks the storage it points to.
#  646|   					  "headers", flags, true);
#  647|   	if (offset == (off_t)-1)
#  648|-> 		return -1;
#  649|   
#  650|   	out_compression_start(handle, compress);', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-40111

', 'Memory allocated by `get_tracing_file` (line 464) for `path` (line 639) is not freed before returning -1 at line 648 when `offset == (off_t)-1`, confirming a resource leak (CWE-772) in this execution path.', '2025-11-18 16:18:56.218934');
INSERT INTO public.ground_truth VALUES (494, 'trace-cmd-3.2-2.el10', 6, 'Error: RESOURCE_LEAK (CWE-772):
trace-cmd-v3.2/lib/trace-cmd/trace-plugin.c:115: alloc_fn: Storage is returned from allocation function "dlopen".
trace-cmd-v3.2/lib/trace-cmd/trace-plugin.c:115: var_assign: Assigning: "handle" = storage returned from "dlopen(plugin, 258)".
trace-cmd-v3.2/lib/trace-cmd/trace-plugin.c:121: noescape: Resource "handle" is not freed or pointed-to in "dlsym".
trace-cmd-v3.2/lib/trace-cmd/trace-plugin.c:125: noescape: Resource "handle" is not freed or pointed-to in "dlsym".
trace-cmd-v3.2/lib/trace-cmd/trace-plugin.c:149: leaked_storage: Variable "handle" going out of scope leaks the storage it points to.
#  147|    out_free:
#  148|   	free(plugin);
#  149|-> }
#  150|   
#  151|   static void', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-40111

', 'Storage allocated by `dlopen` for `handle` (line 115) is not explicitly freed (e.g., via `dlclose`) before going out of scope (line 149), despite being used in `dlsym` calls, and only `plugin` is freed at `out_free` (line 148).', '2025-11-18 16:18:56.218934');
INSERT INTO public.ground_truth VALUES (495, 'trace-cmd-3.2-2.el10', 7, 'Error: UNINIT (CWE-457):
trace-cmd-v3.2/tracecmd/trace-read.c:1176: var_decl: Declaring variable "first_ts" without initializer.
trace-cmd-v3.2/tracecmd/trace-read.c:1283: uninit_use: Using uninitialized value "first_ts".
# 1281|   	if (align_ts) {
# 1282|   		list_for_each_entry(handles, handle_list, list) {
# 1283|-> 			tracecmd_add_ts_offset(handles->handle, -first_ts);
# 1284|   		}
# 1285|   	}', true, 'Non-Issue', 'When we reach the point where we use first_ts second list_for_each_entry(handles, handle_list, list) loop, we know it''s be initialized in the first such loop (same loop, align_ts is true)', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.218934');
INSERT INTO public.ground_truth VALUES (496, 'trace-cmd-3.2-2.el10', 8, 'Error: INTEGER_OVERFLOW (CWE-190):
trace-cmd-v3.2/lib/trace-cmd/trace-compress.c:49: tainted_data_return: Called function "read(fd, dst + size, len)", and a possible return value may be less than zero.
trace-cmd-v3.2/lib/trace-cmd/trace-compress.c:49: cast_overflow: An assign that casts to a different type, which might trigger an overflow.
trace-cmd-v3.2/lib/trace-cmd/trace-compress.c:52: overflow: The expression "len" is considered to have possibly overflowed.
trace-cmd-v3.2/lib/trace-cmd/trace-compress.c:49: overflow_sink: "len", which might be negative, is passed to "read(fd, dst + size, len)". [Note: The source code implementation of the function has been overridden by a builtin model.]
#   47|   
#   48|   	do {
#   49|-> 		r = read(fd, dst+size, len);
#   50|   		if (r > 0) {
#   51|   			size += r;', true, 'Non-Issue', 'We explicitely test that r > 0 and it is always <= len (that''s the purpose of the last argument of read after all). So len - r remain >= 0', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.218934');
INSERT INTO public.ground_truth VALUES (497, 'trace-cmd-3.2-2.el10', 9, 'Error: INTEGER_OVERFLOW (CWE-190):
trace-cmd-v3.2/lib/trace-cmd/trace-recorder.c:330: tainted_data_return: Called function "write(recorder->fd, buf + (r - left), left)", and a possible return value may be less than zero.
trace-cmd-v3.2/lib/trace-cmd/trace-recorder.c:330: assign: Assigning: "w" = "write(recorder->fd, buf + (r - left), left)".
trace-cmd-v3.2/lib/trace-cmd/trace-recorder.c:332: overflow: The expression "left" is considered to have possibly overflowed.
trace-cmd-v3.2/lib/trace-cmd/trace-recorder.c:330: overflow_sink: "left", which might be negative, is passed to "write(recorder->fd, buf + (r - left), left)".
#  328|   	left = r;
#  329|   	do {
#  330|-> 		w = write(recorder->fd, buf + (r - left), left);
#  331|   		if (w > 0) {
#  332|   			left -= w;', true, 'Non-Issue', 'We explicitely test that w > 0 and it is always <= left (that''s the purpose of the last argument of write after all). So left - w remain >= 0', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.218934');
INSERT INTO public.ground_truth VALUES (498, 'trace-cmd-3.2-2.el10', 10, 'Error: RESOURCE_LEAK (CWE-772):
trace-cmd-v3.2/lib/trace-cmd/trace-util.c:278: alloc_fn: Storage is returned from allocation function "realloc".
trace-cmd-v3.2/lib/trace-cmd/trace-util.c:278: var_assign: Assigning: "ptr" = storage returned from "realloc(pdata->files, 8UL * size)".
trace-cmd-v3.2/lib/trace-cmd/trace-util.c:297: leaked_storage: Variable "ptr" going out of scope leaks the storage it points to.
#  295|   	pdata->files = NULL;
#  296|   	pdata->ret = errno;
#  297|-> }
#  298|   
#  299|   /**', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-40111

It''s actually worse. Not only does the code not free ptr if strdup fails, but it tries to use the realloced pointer.', 'Resource leak occurs when `realloc` succeeds but subsequent `strdup` (line 282) fails, causing the newly allocated memory (stored in `pdata->files`) to remain unfreed despite `pdata->files` being set to `NULL` (line 295).', '2025-11-18 16:18:56.218934');
INSERT INTO public.ground_truth VALUES (499, 'trace-cmd-3.2-2.el10', 11, 'Error: BUFFER_SIZE (CWE-170):
trace-cmd-v3.2/lib/trace-cmd/trace-timesync.c:84: buffer_size_warning: Calling "strncpy" with a maximum size argument of 16 bytes on destination array "proto->proto_name" of size 16 bytes might leave the destination string unterminated.
#   82|   	if (!proto)
#   83|   		return -1;
#   84|-> 	strncpy(proto->proto_name, proto_name, TRACECMD_TSYNC_PNAME_LENGTH);
#   85|   	proto->accuracy = accuracy;
#   86|   	proto->roles = roles;', true, 'Non-Issue', 'proto_name is always a constant string of less than 15 characters: TRACECMD_TSYNC_PROTO_NONE, PTP_NAME or KVM_NAME. It''s all part of libtracecmd code and can''t come from anywhare else.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.218934');
INSERT INTO public.ground_truth VALUES (500, 'trace-cmd-3.2-2.el10', 12, 'Error: RESOURCE_LEAK (CWE-772):
trace-cmd-v3.2/lib/trace-cmd/trace-input.c:3786: alloc_fn: Storage is returned from allocation function "malloc".
trace-cmd-v3.2/lib/trace-cmd/trace-input.c:3786: var_assign: Assigning: "buf" = storage returned from "malloc(size)".
trace-cmd-v3.2/lib/trace-cmd/trace-input.c:3791: noescape: Resource "buf" is not freed or pointed-to in "do_read_check".
trace-cmd-v3.2/lib/trace-cmd/trace-input.c:3949: leaked_storage: Variable "buf" going out of scope leaks the storage it points to.
# 3947|   	if (compress)
# 3948|   		in_uncompress_reset(handle);
# 3949|-> 	return ret;
# 3950|   }
# 3951|', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-40111

', 'Memory allocated for `buf` at line 3786 is not guaranteed to be freed in all execution paths, notably at the direct `return ret;` (line 3949) outside the loop containing `free(buf);` (line 3940), confirming a resource leak.', '2025-11-18 16:18:56.218934');
INSERT INTO public.ground_truth VALUES (513, 'trace-cmd-3.2-2.el10', 25, 'Error: RESOURCE_LEAK (CWE-772):
trace-cmd-v3.2/tracecmd/trace-record.c:2234: alloc_arg: "asprintf" allocates memory that is stored into "p". [Note: The source code implementation of the function has been overridden by a builtin model.]
trace-cmd-v3.2/tracecmd/trace-record.c:2237: noescape: Resource "p" is not freed or pointed-to in "stat".
trace-cmd-v3.2/tracecmd/trace-record.c:2242: noescape: Resource "p" is not freed or pointed-to in "read_error_log".
trace-cmd-v3.2/tracecmd/trace-record.c:2254: leaked_storage: Variable "p" going out of scope leaks the storage it points to.
# 2252|   	printf("Failed %s of %s\n", type, file);
# 2253|   	free(path);
# 2254|-> 	return;
# 2255|   }
# 2256|', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-40111

', 'Variable `p`, allocated via `asprintf` at line 2234, is not freed in all execution paths before going out of scope at line 2254, specifically in the successful `stat` and `read_error_log` path, where `free(p)` is only executed if `stat` fails (line 2240).', '2025-11-18 16:18:56.218934');
INSERT INTO public.ground_truth VALUES (501, 'trace-cmd-3.2-2.el10', 13, 'Error: RESOURCE_LEAK (CWE-772):
trace-cmd-v3.2/tracecmd/trace-read.c:433: alloc_fn: Storage is returned from allocation function "strdup".
trace-cmd-v3.2/tracecmd/trace-read.c:433: var_assign: Assigning: "pids" = storage returned from "strdup(arg)".
trace-cmd-v3.2/tracecmd/trace-read.c:441: identity_transfer: Passing "pids" as argument 1 to function "strtok_r", which sets "sav" to an offset off that argument.
trace-cmd-v3.2/tracecmd/trace-read.c:454: leaked_storage: Variable "sav" going out of scope leaks the storage it points to.
trace-cmd-v3.2/tracecmd/trace-read.c:454: leaked_storage: Variable "pids" going out of scope leaks the storage it points to.
#  452|   		pid = strtok_r(NULL, ",", &sav);
#  453|   	}
#  454|-> }
#  455|   
#  456|   static void add_comm_filter(const char *arg)', true, 'Non-Issue', 'The value of pids isn''t lost. It''s the same as the first token returned by strtok_r(). It explicitely marked to-be-freed while the other token, which points to an offset within pids, are not. sav is pointer used internally by strtok_r to keep it''s context in multi-thread environment.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.218934');
INSERT INTO public.ground_truth VALUES (502, 'trace-cmd-3.2-2.el10', 14, 'Error: INTEGER_OVERFLOW (CWE-190):
trace-cmd-v3.2/tracecmd/trace-dump.c:77: tainted_data_return: Called function "read(fd, dst + size, len)", and a possible return value may be less than zero.
trace-cmd-v3.2/tracecmd/trace-dump.c:77: cast_overflow: An assign that casts to a different type, which might trigger an overflow.
trace-cmd-v3.2/tracecmd/trace-dump.c:80: overflow: The expression "len" is considered to have possibly overflowed.
trace-cmd-v3.2/tracecmd/trace-dump.c:77: overflow_sink: "len", which might be negative, is passed to "read(fd, dst + size, len)". [Note: The source code implementation of the function has been overridden by a builtin model.]
#   75|   
#   76|   	do {
#   77|-> 		r = read(fd, dst+size, len);
#   78|   		if (r > 0) {
#   79|   			size += r;', true, 'Non-Issue', 'We explicitely test that r > 0 and it is always <= len (that''s the purpose of the last argument of read after all). So len - r remain >= 0', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.218934');
INSERT INTO public.ground_truth VALUES (503, 'trace-cmd-3.2-2.el10', 15, 'Error: RESOURCE_LEAK (CWE-772):
trace-cmd-v3.2/lib/trace-cmd/trace-input.c:655: alloc_fn: Storage is returned from allocation function "malloc".
trace-cmd-v3.2/lib/trace-cmd/trace-input.c:655: var_assign: Assigning: "buf" = storage returned from "malloc(size + 1)".
trace-cmd-v3.2/lib/trace-cmd/trace-input.c:661: noescape: Resource "buf" is not freed or pointed-to in "strncpy". [Note: The source code implementation of the function has been overridden by a builtin model.]
trace-cmd-v3.2/lib/trace-cmd/trace-input.c:665: noescape: Resource "buf" is not freed or pointed-to in "strtok".
trace-cmd-v3.2/lib/trace-cmd/trace-input.c:667: noescape: Assuming resource "buf" is not freed or pointed-to as ellipsis argument to "tracecmd_warning".
trace-cmd-v3.2/lib/trace-cmd/trace-input.c:668: leaked_storage: Variable "buf" going out of scope leaks the storage it points to.
#  666|   	if (!line) {
#  667|   		tracecmd_warning("No newline found in ''%s''", buf);
#  668|-> 		return 0;
#  669|   	}
#  670|   	/* skip name if it is there */', false, 'Non-Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-40111

', 'The allocated ''buf'' (line 655) is explicitly freed at line 676, outside conditional return paths, ensuring all execution paths free the resource, contradicting the reported resource leak (line 668) vulnerability.', '2025-11-18 16:18:56.218934');
INSERT INTO public.ground_truth VALUES (504, 'trace-cmd-3.2-2.el10', 16, 'Error: RESOURCE_LEAK (CWE-772):
trace-cmd-v3.2/lib/trace-cmd/trace-output.c:918: alloc_fn: Storage is returned from allocation function "strdup".
trace-cmd-v3.2/lib/trace-cmd/trace-output.c:918: var_assign: Assigning: "str" = storage returned from "strdup(list->glob)".
trace-cmd-v3.2/lib/trace-cmd/trace-output.c:923: noescape: Resource "str" is not freed or pointed-to in "strchr".
trace-cmd-v3.2/lib/trace-cmd/trace-output.c:928: noescape: Resource "str" is not freed or pointed-to in "strchr".
trace-cmd-v3.2/lib/trace-cmd/trace-output.c:936: var_assign: Assigning: "ptr" = "str".
trace-cmd-v3.2/lib/trace-cmd/trace-output.c:937: noescape: Resource "ptr" is not freed or pointed-to in "strlen".
trace-cmd-v3.2/lib/trace-cmd/trace-output.c:955: leaked_storage: Variable "ptr" going out of scope leaks the storage it points to.
#  953|    err_mem:
#  954|   	 tracecmd_warning("Insufficient memory");
#  955|-> }
#  956|   
#  957|   static int read_ftrace_files(struct tracecmd_output *handle, bool compress)', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-40111

', 'Memory allocated for `str` via `strdup(list->glob)` at line 918 is not freed in the error handling path (`err_mem`, lines 953-955), specifically when `ptr` (aliasing `str`) goes out of scope at line 955, directly linking to CWE-772: Resource Leak.', '2025-11-18 16:18:56.218934');
INSERT INTO public.ground_truth VALUES (505, 'trace-cmd-3.2-2.el10', 17, 'Error: INTEGER_OVERFLOW (CWE-190):
trace-cmd-v3.2/tracecmd/trace-listen.c:612: tainted_data_return: Called function "write(ofd, buf + s, t)", and a possible return value may be less than zero.
trace-cmd-v3.2/tracecmd/trace-listen.c:612: cast_overflow: An assign that casts to a different type, which might trigger an overflow.
trace-cmd-v3.2/tracecmd/trace-listen.c:619: overflow: The expression "t" is considered to have possibly overflowed.
trace-cmd-v3.2/tracecmd/trace-listen.c:612: overflow_sink: "t", which might be negative, is passed to "write(ofd, buf + s, t)".
#  610|   		s = 0;
#  611|   		do {
#  612|-> 			s = write(ofd, buf+s, t);
#  613|   			if (s < 0) {
#  614|   				if (errno == EINTR)', true, 'Non-Issue', 'We explicitely test that s >= 0 and it is always <= t (that''s the purpose of the last argument of write after all). So t - s remain >= 0', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.218934');
INSERT INTO public.ground_truth VALUES (506, 'trace-cmd-3.2-2.el10', 18, 'Error: RESOURCE_LEAK (CWE-772):
trace-cmd-v3.2/lib/trace-cmd/trace-output.c:1205: alloc_fn: Storage is returned from allocation function "get_tracing_file".
trace-cmd-v3.2/lib/trace-cmd/trace-output.c:1205: var_assign: Assigning: "path" = storage returned from "get_tracing_file(handle, "printk_formats")".
trace-cmd-v3.2/lib/trace-cmd/trace-output.c:1213: leaked_storage: Variable "path" going out of scope leaks the storage it points to.
# 1211|   	offset = out_write_section_header(handle, TRACECMD_OPTION_PRINTK, "printk", flags, true);
# 1212|   	if (offset == (off_t)-1)
# 1213|-> 		return -1;
# 1214|   
# 1215|   	out_compression_start(handle, compress);', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-40111

', 'Variable ''path'' allocated by ''get_tracing_file'' at line 1205 is not explicitly deallocated in the error path (lines 1211-1213) when ''offset'' equals (off_t)-1, directly correlating with a potential resource leak (CWE-772) vulnerability.', '2025-11-18 16:18:56.218934');
INSERT INTO public.ground_truth VALUES (586, 'util-linux-2.40-0.8.rc1.el10', 7, 'Error: RESOURCE_LEAK (CWE-772):
util-linux-2.40-rc1/text-utils/more.c:1243: open_fn: Returning handle opened by "open". [Note: The source code implementation of the function has been overridden by a user model.]
util-linux-2.40-rc1/text-utils/more.c:1243: var_assign: Assigning: "__dummy" = handle returned from "open("/dev/tty", 0)".
util-linux-2.40-rc1/text-utils/more.c:1243: leaked_handle: Handle variable "__dummy" going out of scope leaks the handle.
# 1241|   		if (!isatty(STDIN_FILENO)) {
# 1242|   			close(STDIN_FILENO);
# 1243|-> 			ignore_result( open("/dev/tty", 0) );
# 1244|   		}
# 1245|   		reset_tty(ctl);', true, 'Non-Issue', 'expected and wanted', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.249001');
INSERT INTO public.ground_truth VALUES (507, 'trace-cmd-3.2-2.el10', 19, 'Error: INTEGER_OVERFLOW (CWE-190):
trace-cmd-v3.2/tracecmd/trace-stat.c:84: tainted_data_return: Called function "read(fd, str + total, alloc - total)", and a possible return value may be less than zero.
trace-cmd-v3.2/tracecmd/trace-stat.c:84: cast_overflow: An assign that casts to a different type, which might trigger an overflow.
trace-cmd-v3.2/tracecmd/trace-stat.c:87: overflow: The expression "total" is considered to have possibly overflowed.
trace-cmd-v3.2/tracecmd/trace-stat.c:80: overflow: The expression "total + 8192UL" is deemed overflowed because at least one of its arguments has overflowed.
trace-cmd-v3.2/tracecmd/trace-stat.c:80: overflow: The expression "(total + 8192UL) / 8192UL" is deemed underflowed because at least one of its arguments has underflowed.
trace-cmd-v3.2/tracecmd/trace-stat.c:80: overflow: The expression "(total + 8192UL) / 8192UL * 8192UL" is deemed underflowed because at least one of its arguments has underflowed.
trace-cmd-v3.2/tracecmd/trace-stat.c:80: assign: Assigning: "alloc" = "(total + 8192UL) / 8192UL * 8192UL".
trace-cmd-v3.2/tracecmd/trace-stat.c:81: overflow: The expression "alloc + 1UL" is deemed underflowed because at least one of its arguments has underflowed.
trace-cmd-v3.2/tracecmd/trace-stat.c:81: overflow_sink: "alloc + 1UL", which might have underflowed, is passed to "realloc(str, alloc + 1UL)".
#   79|   	for (;;) {
#   80|   		alloc = ((total + BUFSIZ) / BUFSIZ) * BUFSIZ;
#   81|-> 		str = realloc(str, alloc + 1);
#   82|   		if (!str)
#   83|   			die("malloc");', true, 'Issue', 'We explicitely check that ret >= 0 and we know that ret <= alloc - total', 'Potential integer overflow in `total` (lines 80, 87) and unclear handling of underflow in `alloc` calculation (lines 80, 81) pose risks, directly correlating with CWE-190, as the code lacks explicit safeguards against these scenarios, particularly if `ret < 0` (line 85) or `total` overflows upon incrementation.', '2025-11-18 16:18:56.218934');
INSERT INTO public.ground_truth VALUES (508, 'trace-cmd-3.2-2.el10', 20, 'Error: INTEGER_OVERFLOW (CWE-190):
trace-cmd-v3.2/tracecmd/trace-listen.c:215: tainted_data_return: Called function "write(fd, buf + (r - left), left)", and a possible return value may be less than zero.
trace-cmd-v3.2/tracecmd/trace-listen.c:215: cast_overflow: An assign that casts to a different type, which might trigger an overflow.
trace-cmd-v3.2/tracecmd/trace-listen.c:217: overflow: The expression "left" is considered to have possibly overflowed.
trace-cmd-v3.2/tracecmd/trace-listen.c:215: overflow_sink: "left", which might be negative, is passed to "write(fd, buf + (r - left), left)".
#  213|   		left = r;
#  214|   		do {
#  215|-> 			w = write(fd, buf + (r - left), left);
#  216|   			if (w > 0)
#  217|   				left -= w;', true, 'Non-Issue', 'We explicitely test that w > 0 and it is always <= left (that''s the purpose of the last argument of write after all). So left - w remain >= 0', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.218934');
INSERT INTO public.ground_truth VALUES (509, 'trace-cmd-3.2-2.el10', 21, 'Error: COMPILER_WARNING:
trace-cmd-v3.2/tracecmd/trace-read.c: scope_hint: In function ‘trace_report’
trace-cmd-v3.2/tracecmd/trace-read.c:485:60: warning[-Wformat-overflow=]: ‘%s’ directive argument is null
#  485 |                         die("Failed to allocate for filter %s", curr_filter);
#      |                                                            ^~
#  483|   		filter = malloc(len);
#  484|   		if (!filter)
#  485|-> 			die("Failed to allocate for filter %s", curr_filter);
#  486|   		sprintf(filter, ".*:" FILTER_FMT, pid, pid, pid);
#  487|   	} else {', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-40111

', 'A null `curr_filter` can be passed to `die()` with a `%s` format specifier at line 485 if `malloc(len)` fails and initial `curr_filter` is null, directly correlating with the CVE''s described vulnerability of a null argument for the `%s` directive.', '2025-11-18 16:18:56.218934');
INSERT INTO public.ground_truth VALUES (510, 'trace-cmd-3.2-2.el10', 22, 'Error: RESOURCE_LEAK (CWE-772):
trace-cmd-v3.2/lib/trace-cmd/trace-output.c:884: alloc_fn: Storage is returned from allocation function "strdup".
trace-cmd-v3.2/lib/trace-cmd/trace-output.c:884: var_assign: Assigning: "system" = storage returned from "strdup(file + events_len + 1)".
trace-cmd-v3.2/lib/trace-cmd/trace-output.c:885: identity_transfer: Passing "system" as argument 1 to function "strtok_r", which sets "ptr" to an offset off that argument.
trace-cmd-v3.2/lib/trace-cmd/trace-output.c:885: overwrite_var: Overwriting "ptr" in call to "strtok_r" leaks the storage that "ptr" points to.
#  883|   		file = globbuf.gl_pathv[i];
#  884|   		system = strdup(file + events_len + 1);
#  885|-> 		system = strtok_r(system, "/", &ptr);
#  886|   		if (!ptr) {
#  887|   			/* ?? should we warn? */', true, 'Non-Issue', 'After the call to strtok_r() system still points to the same point as before the call (first token). It is freed in the same iteration of the loop. The fact that ptr or event point to an offset of system doesn''t matter. The storage get freed all the same when free(system) is called.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.218934');
INSERT INTO public.ground_truth VALUES (511, 'trace-cmd-3.2-2.el10', 23, 'Error: RESOURCE_LEAK (CWE-772):
trace-cmd-v3.2/tracecmd/trace-record.c:1220: alloc_fn: Storage is returned from allocation function "realloc".
trace-cmd-v3.2/tracecmd/trace-record.c:1220: var_assign: Assigning: "map" = storage returned from "realloc(maps->lib_maps, (maps->nr_lib_maps + 1U) * 24UL)".
trace-cmd-v3.2/tracecmd/trace-record.c:1260: leaked_storage: Variable "map" going out of scope leaks the storage it points to.
# 1258|   		free(maps);
# 1259|   	}
# 1260|-> 	return ret;
# 1261|   }
# 1262|', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-40111

It actually worse: there is a risk of memory corruption since we try to access the original pointer that was reallocated.', 'Memory allocated for ''lib_maps'' via realloc at line 1220 is not explicitly freed in all execution paths, notably in the error path leading to line 1260, despite ''maps'' being freed, causing a potential memory leak.', '2025-11-18 16:18:56.218934');
INSERT INTO public.ground_truth VALUES (512, 'trace-cmd-3.2-2.el10', 24, 'Error: RESOURCE_LEAK (CWE-772):
trace-cmd-v3.2/tracecmd/trace-hist.c:1041: alloc_fn: Storage is returned from allocation function "tracecmd_alloc".
trace-cmd-v3.2/tracecmd/trace-hist.c:1041: var_assign: Assigning: "handle" = storage returned from "tracecmd_alloc(input_file, 0)".
trace-cmd-v3.2/tracecmd/trace-hist.c:1045: noescape: Resource "handle" is not freed or pointed-to in "tracecmd_read_headers".
trace-cmd-v3.2/tracecmd/trace-hist.c:1047: leaked_storage: Variable "handle" going out of scope leaks the storage it points to.
# 1045|   	ret = tracecmd_read_headers(handle, 0);
# 1046|   	if (ret)
# 1047|-> 		return;
# 1048|   
# 1049|   	ret = tracecmd_init_data(handle);', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-40111

Probably not a big deal since in this case, the program will exit soon anyway.', 'Storage allocated by `tracecmd_alloc` at line 1041 is not freed before going out of scope at line 1047 when `tracecmd_read_headers` returns a non-zero value, due to the absence of a deallocation call in this error path.', '2025-11-18 16:18:56.218934');
INSERT INTO public.ground_truth VALUES (595, 'util-linux-2.40-0.8.rc1.el10', 16, 'Error: VARARGS (CWE-237):
util-linux-2.40-rc1/lib/strv.c:92: va_arg: Calling va_arg on va_list "aq", which has not been prepared with va_start().
#   90|   
#   91|                   va_copy(aq, ap);
#   92|->                 while ((s = va_arg(aq, const char*))) {
#   93|                           if (s == (const char*) -1)
#   94|                                   continue;', true, 'Non-Issue', 'va_list prepared by va_copy()', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.249001');
INSERT INTO public.ground_truth VALUES (514, 'trace-cmd-3.2-2.el10', 26, 'Error: INTEGER_OVERFLOW (CWE-190):
trace-cmd-v3.2/tracecmd/trace-record.c:2727: tainted_data_return: Called function "write(fd, str, len)", and a possible return value may be less than zero.
trace-cmd-v3.2/tracecmd/trace-record.c:2727: cast_overflow: An assign that casts to a different type, which might trigger an overflow.
trace-cmd-v3.2/tracecmd/trace-record.c:2731: overflow: The expression "len" is considered to have possibly overflowed.
trace-cmd-v3.2/tracecmd/trace-record.c:2727: overflow_sink: "len", which might be negative, is passed to "write(fd, str, len)".
# 2725|   	str = filter;
# 2726|   	do {
# 2727|-> 		ret = write(fd, str, len);
# 2728|   		if (ret < 0)
# 2729|   			die("Failed to write to set_event_pid");', true, 'Non-Issue', 'We explicitely test that ret > 0 and it is always <= len (that''s the purpose of the last argument of write after all). So len - ret remain >= 0', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.218934');
INSERT INTO public.ground_truth VALUES (515, 'trace-cmd-3.2-2.el10', 27, 'Error: RESOURCE_LEAK (CWE-772):
trace-cmd-v3.2/tracecmd/trace-record.c:2708: alloc_fn: Storage is returned from allocation function "malloc".
trace-cmd-v3.2/tracecmd/trace-record.c:2708: var_assign: Assigning: "filter" = storage returned from "malloc(len)".
trace-cmd-v3.2/tracecmd/trace-record.c:2712: var_assign: Assigning: "str" = "filter".
trace-cmd-v3.2/tracecmd/trace-record.c:2736: leaked_storage: Variable "str" going out of scope leaks the storage it points to.
trace-cmd-v3.2/tracecmd/trace-record.c:2736: leaked_storage: Variable "filter" going out of scope leaks the storage it points to.
# 2734|    out:
# 2735|   	close(fd);
# 2736|-> }
# 2737|   
# 2738|   static void update_pid_event_filters(struct buffer_instance *instance)', false, 'Non-Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-40111

', 'Variables ''str'' and ''filter'' are assigned the same allocated memory, but the reported leak at line 2736 overlooks implicit memory reclamation via process termination on error (''die'' function calls) and the conditional function flow, mitigating the traditional memory leak concern.', '2025-11-18 16:18:56.218934');
INSERT INTO public.ground_truth VALUES (516, 'trace-cmd-3.2-2.el10', 28, 'Error: OVERRUN (CWE-119):
trace-cmd-v3.2/lib/trace-cmd/trace-output.c:2064: return_constant: Function call "do_lseek(&out_handle, 0L, 1)" may return -1.
trace-cmd-v3.2/lib/trace-cmd/trace-output.c:2064: assignment: Assigning: "offset" = "do_lseek(&out_handle, 0L, 1)". The value of "offset" is now 18446744073709551615.
trace-cmd-v3.2/lib/trace-cmd/trace-output.c:2071: overrun-buffer-arg: Calling "read" with "buf" and "offset" is suspicious because of the very large index, 18446744073709551615. The index may be due to a negative parameter being interpreted as unsigned. [Note: The source code implementation of the function has been overridden by a builtin model.]
# 2069|           if (do_lseek(&out_handle, 0, SEEK_SET) == (off_t)-1)
# 2070|                   goto out;
# 2071|->         *len = read(msg_handle.cfd, buf, offset);
# 2072|           if (*len != offset) {
# 2073|                   free(buf);', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-40111

Probably not an overrun though: malloc(-1) would fail before it reaches read()', 'Unvalidated `do_lseek` return value (-1) is assigned to `offset`, potentially leading to a buffer overrun in the subsequent `read` call (line 2071) with an enormous `count` value (18446744073709551615), as the error check at line 2069 only applies to `SEEK_SET`, not the vulnerable `SEEK_CUR` call at line 2064.', '2025-11-18 16:18:56.218934');
INSERT INTO public.ground_truth VALUES (517, 'trace-cmd-3.2-2.el10', 29, 'Error: USE_AFTER_FREE (CWE-416):
trace-cmd-v3.2/lib/trace-cmd/trace-output.c:686: closed_arg: "close(int)" closes "fd".
trace-cmd-v3.2/lib/trace-cmd/trace-output.c:730: double_close: Calling "close(int)" closes handle "fd" which has already been closed.
#  728|           out_compression_reset(handle, compress);
#  729|           if (fd >= 0)
#  730|->                 close(fd);
#  731|           return -1;
#  732|   }', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-40111

', 'Potential double-close vulnerability exists due to `close(fd)` at line 686 and a conditional second `close(fd)` at line 730 in the `out_close` error path, without guaranteeing `fd` is open before the second call, aligning with the CVE''s **double_close** description.', '2025-11-18 16:18:56.218934');
INSERT INTO public.ground_truth VALUES (518, 'trace-cmd-3.2-2.el10', 30, 'Error: RESOURCE_LEAK (CWE-772):
trace-cmd-v3.2/tracecmd/trace-listen.c:334: open_arg: "trace_net_search" opens handle stored into "sfd".
trace-cmd-v3.2/tracecmd/trace-listen.c:336: leaked_handle: Handle variable "sfd" going out of scope leaks the handle.
#  334|   	num_port = trace_net_search(start_port, &sfd, type);
#  335|   	if (num_port < 0)
#  336|-> 		return num_port;
#  337|   
#  338|   	fork_reader(sfd, node, port, pid, cpu, pagesize, type);', true, 'Non-Issue', 'trace_net_search only allocates a descriptor if it is sucessful', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.218934');
INSERT INTO public.ground_truth VALUES (519, 'trace-cmd-3.2-2.el10', 31, 'Error: RESOURCE_LEAK (CWE-772):
trace-cmd-v3.2/lib/trace-cmd/trace-timesync-ptp.c:607: alloc_arg: "tracecmd_msg_recv_time_sync" allocates memory that is stored into "results".
trace-cmd-v3.2/lib/trace-cmd/trace-timesync-ptp.c:612: leaked_storage: Variable "results" going out of scope leaks the storage it points to.
#  610|   	if (ret || strncmp(sync_proto, PTP_NAME, TRACECMD_TSYNC_PNAME_LENGTH) ||
#  611|   	    sync_msg != PTP_SYNC_PKT_PROBES || size == 0 || results == NULL)
#  612|-> 		return -1;
#  613|   
#  614|   	ntoh_ptp_results(results);', false, 'Non-Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-40111

', 'Memory allocated for ''results'' is explicitly freed at line 660, contradicting the reported memory leak concern at line 612, as the free operation occurs within the same function scope after the potentially vulnerable return statement.', '2025-11-18 16:18:56.218934');
INSERT INTO public.ground_truth VALUES (520, 'trace-cmd-3.2-2.el10', 32, 'Error: IDENTICAL_BRANCHES (CWE-398):
trace-cmd-v3.2/lib/trace-cmd/trace-util.c:466: identical_branches: The same code is executed regardless of whether "newline" is true, because the ''then'' and ''else'' branches are identical. Should one of the branches be modified, or the entire ''if'' statement replaced?
#  464|   
#  465|   	if (logfp) {
#  466|-> 		if (newline)
#  467|   			fprintf(logfp, "[%d]%s%.*s", getpid(), prefix, r, buf);
#  468|   		else', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-40111

', 'The `if (newline)` statement at line 466 has identical ''then'' and ''else'' branches (lines 467 and 468-469), executing the same `fprintf` statement with unmodified arguments, indicating a potential logical error with no explicit evidence of intentional design.', '2025-11-18 16:18:56.218934');
INSERT INTO public.ground_truth VALUES (533, 'trace-cmd-3.2-2.el10', 45, 'Error: COMPILER_WARNING:
trace-cmd-v3.2/tracecmd/trace-record.c: scope_hint: In function ‘get_temp_file’
trace-cmd-v3.2/tracecmd/trace-record.c:556:63: warning[-Wformat-overflow=]: ‘%s’ directive argument is null
#  556 |                         die("Failed to allocate temp file for %s", name);
#      |                                                               ^~
#  554|   		file = malloc(size + 1);
#  555|   		if (!file)
#  556|-> 			die("Failed to allocate temp file for %s", name);
#  557|   		sprintf(file, "%s.cpu%d", output_file, cpu);
#  558|   	}', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-40111

', 'A plausible execution path exists where `name` can be NULL (if `tracefs_instance_get_name()` fails, line 545) and is passed to `die()` at line 556 without re-validation, potentially causing a null pointer dereference in a format string, correlating with the CVE''s described vulnerability.', '2025-11-18 16:18:56.218934');
INSERT INTO public.ground_truth VALUES (521, 'trace-cmd-3.2-2.el10', 33, 'Error: RESOURCE_LEAK (CWE-772):
trace-cmd-v3.2/tracecmd/trace-record.c:419: alloc_fn: Storage is returned from allocation function "strdup".
trace-cmd-v3.2/tracecmd/trace-record.c:419: var_assign: Assigning: "name" = storage returned from "strdup(dent->d_name)".
trace-cmd-v3.2/tracecmd/trace-record.c:423: noescape: Resource "name" is not freed or pointed-to in "strcmp".
trace-cmd-v3.2/tracecmd/trace-record.c:423: noescape: Resource "name" is not freed or pointed-to in "strcmp".
trace-cmd-v3.2/tracecmd/trace-record.c:427: noescape: Resource "name" is not freed or pointed-to in "append_file".
trace-cmd-v3.2/tracecmd/trace-record.c:431: leaked_storage: Variable "name" going out of scope leaks the storage it points to.
#  429|   		if (ret < 0 || !S_ISDIR(st.st_mode)) {
#  430|   			free(instance_path);
#  431|-> 			continue;
#  432|   		}
#  433|   		free(instance_path);', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-40111

', 'Memory allocated for `name` via `strdup(dent->d_name)` at line 419 is not freed in all execution paths, notably when `continue` is executed at line 431 due to the condition at lines 429-430, directly correlating with the CVE''s leaked_storage vulnerability.', '2025-11-18 16:18:56.218934');
INSERT INTO public.ground_truth VALUES (522, 'trace-cmd-3.2-2.el10', 34, 'Error: INTEGER_OVERFLOW (CWE-190):
trace-cmd-v3.2/lib/trace-cmd/trace-compress.c:716: tainted_data_return: Called function "read(fd, buf_from + all, rchunk - all)", and a possible return value may be less than zero.
trace-cmd-v3.2/lib/trace-cmd/trace-compress.c:716: cast_underflow: An assign of a possibly negative number to an unsigned type, which might trigger an underflow.
trace-cmd-v3.2/lib/trace-cmd/trace-compress.c:717: overflow: The expression "all += r" is deemed underflowed because at least one of its arguments has underflowed.
trace-cmd-v3.2/lib/trace-cmd/trace-compress.c:726: overflow: The expression "rsize += all" is deemed underflowed because at least one of its arguments has underflowed.
trace-cmd-v3.2/lib/trace-cmd/trace-compress.c:711: overflow: The expression "rmax - rsize" is deemed underflowed because at least one of its arguments has underflowed.
trace-cmd-v3.2/lib/trace-cmd/trace-compress.c:711: assign: Assigning: "rchunk" = "rmax - rsize".
trace-cmd-v3.2/lib/trace-cmd/trace-compress.c:716: overflow: The expression "rchunk - all" is deemed underflowed because at least one of its arguments has underflowed.
trace-cmd-v3.2/lib/trace-cmd/trace-compress.c:716: overflow_sink: "rchunk - all", which might have underflowed, is passed to "read(fd, buf_from + all, rchunk - all)". [Note: The source code implementation of the function has been overridden by a builtin model.]
#  714|   
#  715|                   do {
#  716|->                         r = read(fd, buf_from + all, rchunk - all);
#  717|                           all += r;
#  718|', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-40111

We break out of the inner loop if r<=0 and immediately of the second loop if r<0, so no overflow would happen.However, we still add r (possibly -1) to all possibly (1) and exit the loop thinking it succeeded, wich it didn''t.', 'Unsigned variables `r`, `rchunk`, and `all` can potentially underflow/overflow due to assignments from possibly negative `read()` return values (line 716) or unsigned subtraction (line 711), with no explicit mitigations in the provided code, leading to incorrect behavior in subsequent operations (lines 717, 726).', '2025-11-18 16:18:56.218934');
INSERT INTO public.ground_truth VALUES (523, 'trace-cmd-3.2-2.el10', 35, 'Error: INTEGER_OVERFLOW (CWE-190):
trace-cmd-v3.2/tracecmd/trace-read.c:1378: tainted_data_return: Called function "read(fd, dst + size, len)", and a possible return value may be less than zero.
trace-cmd-v3.2/tracecmd/trace-read.c:1378: cast_overflow: An assign that casts to a different type, which might trigger an overflow.
trace-cmd-v3.2/tracecmd/trace-read.c:1381: overflow: The expression "len" is considered to have possibly overflowed.
trace-cmd-v3.2/tracecmd/trace-read.c:1378: overflow_sink: "len", which might be negative, is passed to "read(fd, dst + size, len)". [Note: The source code implementation of the function has been overridden by a builtin model.]
# 1376|   
# 1377|   	do {
# 1378|-> 		r = read(fd, dst+size, len);
# 1379|   		if (r > 0) {
# 1380|   			size += r;', true, 'Non-Issue', 'We explicitely test that r > 0 and it is always <= len (that''s the purpose of the last argument of read after all). So len - r remain >= 0', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.218934');
INSERT INTO public.ground_truth VALUES (524, 'trace-cmd-3.2-2.el10', 36, 'Error: RESOURCE_LEAK (CWE-772):
trace-cmd-v3.2/tracecmd/trace-record.c:2347: alloc_fn: Storage is returned from allocation function "read_file".
trace-cmd-v3.2/tracecmd/trace-record.c:2347: var_assign: Assigning: "buf" = storage returned from "read_file(file)".
trace-cmd-v3.2/tracecmd/trace-record.c:2360: noescape: Resource "buf" is not freed or pointed-to in "strtok".
trace-cmd-v3.2/tracecmd/trace-record.c:2385: leaked_storage: Variable "buf" going out of scope leaks the storage it points to.
# 2383|   		write_file(file, filter);
# 2384|   	}
# 2385|-> }
# 2386|   
# 2387|   static void update_reset_triggers(void)', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-40111

', 'Storage allocated for `buf` at line 2347 is used in a loop (line 2360) without evidence of freeing, and subsequently leaks when `buf` goes out of scope at line 2385, directly correlating with CWE-772 (Resource Leak) vulnerability.', '2025-11-18 16:18:56.218934');
INSERT INTO public.ground_truth VALUES (525, 'trace-cmd-3.2-2.el10', 37, 'Error: RESOURCE_LEAK (CWE-772):
trace-cmd-v3.2/lib/trace-cmd/trace-output.c:694: alloc_fn: Storage is returned from allocation function "get_tracing_file".
trace-cmd-v3.2/lib/trace-cmd/trace-output.c:694: var_assign: Assigning: "path" = storage returned from "get_tracing_file(handle, "events/header_event")".
trace-cmd-v3.2/lib/trace-cmd/trace-output.c:698: noescape: Resource "path" is not freed or pointed-to in "open". [Note: The source code implementation of the function has been overridden by a user model.]
trace-cmd-v3.2/lib/trace-cmd/trace-output.c:700: noescape: Assuming resource "path" is not freed or pointed-to as ellipsis argument to "tracecmd_warning".
trace-cmd-v3.2/lib/trace-cmd/trace-output.c:731: leaked_storage: Variable "path" going out of scope leaks the storage it points to.
#  729|   	if (fd >= 0)
#  730|   		close(fd);
#  731|-> 	return -1;
#  732|   }
#  733|', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-40111

', 'Variable `path` is dynamically allocated at line 694 and leaks upon going out of scope at line 731, with no explicit deallocation in the provided code, confirming a storage leak in at least one execution path.', '2025-11-18 16:18:56.218934');
INSERT INTO public.ground_truth VALUES (526, 'trace-cmd-3.2-2.el10', 38, 'Error: OVERRUN (CWE-119):
trace-cmd-v3.2/tracecmd/trace-dump.c:964: buffer_alloc: Calling allocating function "calloc" which allocated "1UL" items of size "size" bytes.
trace-cmd-v3.2/tracecmd/trace-dump.c:964: var_assign: Assigning: "clock" = "calloc(1UL, size)".
trace-cmd-v3.2/tracecmd/trace-dump.c:970: overrun-local: Overrunning dynamic array "clock" at offset corresponding to index variable "size".
#  968|   	if (read_file_bytes(fd, clock, size))
#  969|   		die("cannot read clock %lld bytes", size);
#  970|-> 	clock[size] = 0;
#  971|   	do_print((SUMMARY | CLOCK), "\t\t%s\n", clock);
#  972|   	free(clock);', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-40111

', 'Code explicitly overruns allocated buffer bounds by assigning to `clock[size]` (line 970) without bounds checking, where `clock` is allocated with `size` bytes (line 964), directly correlating with the reported CVE buffer overrun vulnerability.', '2025-11-18 16:18:56.218934');
INSERT INTO public.ground_truth VALUES (548, 'trace-cmd-3.2-2.el10', 60, 'Error: USE_AFTER_FREE (CWE-416):
trace-cmd-v3.2/tracecmd/trace-record.c:6588: assign: Assigning: "del_list" = "ctx->instance".
trace-cmd-v3.2/tracecmd/trace-record.c:6738: freed_arg: "remove_instances" frees "del_list".
trace-cmd-v3.2/tracecmd/trace-record.c:6750: deref_after_free: Dereferencing freed pointer "ctx->instance".
# 6748|           }
# 6749|   
# 6750|->         if (!ctx->filtered && ctx->instance->filter_mod)
# 6751|                   add_func(&ctx->instance->filter_funcs,
# 6752|                            ctx->instance->filter_mod, "*");', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-40111

Can be triggered by the following: trace-cmd record -v -e block -B foo  ls', 'This is a default value, if it''s not replaced, something went wrong', '2025-11-18 16:18:56.218934');
INSERT INTO public.ground_truth VALUES (527, 'trace-cmd-3.2-2.el10', 39, 'Error: INTEGER_OVERFLOW (CWE-190):
trace-cmd-v3.2/lib/trace-cmd/include/trace-write-local.h:17: tainted_data_return: Called function "write(fd, data + tot, size - tot)", and a possible return value may be less than zero.
trace-cmd-v3.2/lib/trace-cmd/include/trace-write-local.h:17: assign: Assigning: "w" = "write(fd, data + tot, size - tot)".
trace-cmd-v3.2/lib/trace-cmd/include/trace-write-local.h:18: overflow: The expression "tot" is considered to have possibly overflowed.
trace-cmd-v3.2/lib/trace-cmd/include/trace-write-local.h:17: overflow: The expression "size - tot" is deemed overflowed because at least one of its arguments has overflowed.
trace-cmd-v3.2/lib/trace-cmd/include/trace-write-local.h:17: overflow_sink: "size - tot", which might have underflowed, is passed to "write(fd, data + tot, size - tot)".
#   15|   
#   16|   	do {
#   17|-> 		w = write(fd, data + tot, size - tot);
#   18|   		tot += w;
#   19|', true, 'Non-Issue', 'We return right away if w < 0. The possibly overfowed expression is never used.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.218934');
INSERT INTO public.ground_truth VALUES (528, 'trace-cmd-3.2-2.el10', 40, 'Error: OVERRUN (CWE-119):
trace-cmd-v3.2/lib/trace-cmd/trace-input.c:446: buffer_alloc: Calling allocating function "realloc" which allocates "size" bytes.
trace-cmd-v3.2/lib/trace-cmd/trace-input.c:446: var_assign: Assigning: "str" = "realloc(str, size)".
trace-cmd-v3.2/lib/trace-cmd/trace-input.c:450: overrun-local: Overrunning dynamic array "str" at offset corresponding to index variable "size".
#  448|   			return NULL;
#  449|   		memcpy(str + (size - i), buf, i);
#  450|-> 		str[size] = 0;
#  451|   	} else {
#  452|   		size = i + 1;', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-40111

', 'Assignment at `str[size] = 0;` (line 450) is out of bounds for an array of size `size`, directly correlating with the CVE''s description of overrunning the dynamic array `str`, with no intervening conditionals or error handling to prevent this vulnerability (lines 446-450).', '2025-11-18 16:18:56.218934');
INSERT INTO public.ground_truth VALUES (529, 'trace-cmd-3.2-2.el10', 41, 'Error: RESOURCE_LEAK (CWE-772):
trace-cmd-v3.2/tracecmd/trace-record.c:419: alloc_fn: Storage is returned from allocation function "strdup".
trace-cmd-v3.2/tracecmd/trace-record.c:419: var_assign: Assigning: "name" = storage returned from "strdup(dent->d_name)".
trace-cmd-v3.2/tracecmd/trace-record.c:423: noescape: Resource "name" is not freed or pointed-to in "strcmp".
trace-cmd-v3.2/tracecmd/trace-record.c:425: leaked_storage: Variable "name" going out of scope leaks the storage it points to.
#  423|   		if (strcmp(name, ".") == 0 ||
#  424|   		    strcmp(name, "..") == 0)
#  425|-> 			continue;
#  426|   
#  427|   		instance_path = append_file(instances_dir, name);', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-40111

', 'Memory allocated for `name` via `strdup(dent->d_name)` at line 419 is not freed before going out of scope when the `continue` statement is executed at line 425, following the conditional check at lines 423-424, confirming a memory leak in this execution path.', '2025-11-18 16:18:56.218934');
INSERT INTO public.ground_truth VALUES (530, 'trace-cmd-3.2-2.el10', 42, 'Error: RESOURCE_LEAK (CWE-772):
trace-cmd-v3.2/tracecmd/trace-vsock.c:39: open_fn: Returning handle opened by "socket".
trace-cmd-v3.2/tracecmd/trace-vsock.c:39: var_assign: Assigning: "sd" = handle returned from "socket(40, SOCK_STREAM, 0)".
trace-cmd-v3.2/tracecmd/trace-vsock.c:43: noescape: Resource "sd" is not freed or pointed-to in "setsockopt".
trace-cmd-v3.2/tracecmd/trace-vsock.c:45: noescape: Resource "sd" is not freed or pointed-to in "bind".
trace-cmd-v3.2/tracecmd/trace-vsock.c:46: leaked_handle: Handle variable "sd" going out of scope leaks the handle.
#   44|   
#   45|   	if (bind(sd, (struct sockaddr *)&addr, sizeof(addr)))
#   46|-> 		return -errno;
#   47|   
#   48|   	if (listen(sd, SOMAXCONN))', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-40111

', 'Handle ''sd'' is not closed before going out of scope when ''bind'' fails at line 45, directly leading to a ''leaked_handle'' vulnerability as observed in the error return at line 46.', '2025-11-18 16:18:56.218934');
INSERT INTO public.ground_truth VALUES (531, 'trace-cmd-3.2-2.el10', 43, 'Error: INTEGER_OVERFLOW (CWE-190):
trace-cmd-v3.2/tracecmd/trace-stat.c:84: tainted_data_return: Called function "read(fd, str + total, alloc - total)", and a possible return value may be less than zero.
trace-cmd-v3.2/tracecmd/trace-stat.c:84: cast_overflow: An assign that casts to a different type, which might trigger an overflow.
trace-cmd-v3.2/tracecmd/trace-stat.c:87: overflow: The expression "total" is considered to have possibly overflowed.
trace-cmd-v3.2/tracecmd/trace-stat.c:80: overflow: The expression "total + 8192UL" is deemed overflowed because at least one of its arguments has overflowed.
trace-cmd-v3.2/tracecmd/trace-stat.c:80: overflow: The expression "(total + 8192UL) / 8192UL" is deemed underflowed because at least one of its arguments has underflowed.
trace-cmd-v3.2/tracecmd/trace-stat.c:80: overflow: The expression "(total + 8192UL) / 8192UL * 8192UL" is deemed underflowed because at least one of its arguments has underflowed.
trace-cmd-v3.2/tracecmd/trace-stat.c:80: assign: Assigning: "alloc" = "(total + 8192UL) / 8192UL * 8192UL".
trace-cmd-v3.2/tracecmd/trace-stat.c:84: overflow: The expression "alloc - total" is deemed underflowed because at least one of its arguments has underflowed.
trace-cmd-v3.2/tracecmd/trace-stat.c:84: overflow_sink: "alloc - total", which might have underflowed, is passed to "read(fd, str + total, alloc - total)". [Note: The source code implementation of the function has been overridden by a builtin model.]
#   82|   		if (!str)
#   83|   			die("malloc");
#   84|-> 		ret = read(fd, str + total, alloc - total);
#   85|   		if (ret < 0)
#   86|   			die("reading %s\n", file);', true, 'Non-Issue', 'We exit the program immediately if ret < 0', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.218934');
INSERT INTO public.ground_truth VALUES (532, 'trace-cmd-3.2-2.el10', 44, 'Error: RESOURCE_LEAK (CWE-772):
trace-cmd-v3.2/lib/trace-cmd/trace-output.c:862: alloc_fn: Storage is returned from allocation function "get_tracing_file".
trace-cmd-v3.2/lib/trace-cmd/trace-output.c:862: var_assign: Assigning: "events_path" = storage returned from "get_tracing_file(handle, "events")".
trace-cmd-v3.2/lib/trace-cmd/trace-output.c:863: noescape: Resource "events_path" is not freed or pointed-to in "strlen".
trace-cmd-v3.2/lib/trace-cmd/trace-output.c:868: leaked_storage: Variable "events_path" going out of scope leaks the storage it points to.
#  866|   		      strlen("/format") + 2);
#  867|   	if (!path)
#  868|-> 		return;
#  869|   	path[0] = ''\0'';
#  870|   	strcat(path, events_path);', false, 'Non-Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-40111

', 'The reported resource leak at line 868 is mitigated by the function''s early return in the error path (`if (!path)`), and while `events_path` freeing is conditional on `path` allocation success, the specific leak instance at line 868 is contextually prevented, aligning with FALSE POSITIVE under strict guideline analysis.', '2025-11-18 16:18:56.218934');
INSERT INTO public.ground_truth VALUES (81, 'cpio-2.15-1.el10', 7, 'Error: BUFFER_SIZE (CWE-120):
cpio-2.15/src/tar.c:215: buffer_size: Calling "strncpy" with a source string whose length (2 chars) is greater than or equal to the size argument (2) will fail to null-terminate "tar_hdr->version".
#  213|   
#  214|         strncpy (tar_hdr->magic, TMAGIC, TMAGLEN);
#  215|->       strncpy (tar_hdr->version, TVERSION, TVERSLEN);
#  216|   
#  217|         name = getuser (file_hdr->c_uid);', true, 'Non-Issue', 'tar format counts with that and the tar header was zeroed before (line 141)', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.051962');
INSERT INTO public.ground_truth VALUES (534, 'trace-cmd-3.2-2.el10', 46, 'Error: COMPILER_WARNING:
trace-cmd-v3.2/tracecmd/trace-stream.c: scope_hint: In function ‘trace_stream_read’
trace-cmd-v3.2/tracecmd/trace-stream.c:135:15: warning[-Wstringop-overflow=]: ‘poll’ specified size 18446744056529682432 exceeds maximum object size 9223372036854775807
/usr/include/poll.h:1: included_from: Included from here.
trace-cmd-v3.2/tracecmd/trace-stream.c:7: included_from: Included from here.
/usr/include/sys/poll.h:54:12: note: in a call to function ‘poll’ declared with attribute ‘access (write_only, 1, 2)’
#  133|           }
#  134|   
#  135|->         ret = poll(pollfd, nr_pids, sleep_ms);
#  136|           if (ret > 0)
#  137|                   goto again;', true, 'Issue', 'I can''t get this warning when compiling the code. It looks like an artifact of the analyser as if it tries to compile the code with nr_pids=ULLONG_MAX or something.', 'The `poll` function at `trace-stream.c:135` is vulnerable to a potential buffer overflow due to an excessively large `nr_pids` value (18446744056529682432), which exceeds the maximum object size, and lacks explicit bounds checking in the `trace_stream_read` function beyond a zero check at `trace-stream.c:97-98`.', '2025-11-18 16:18:56.218934');
INSERT INTO public.ground_truth VALUES (535, 'trace-cmd-3.2-2.el10', 47, 'Error: RESOURCE_LEAK (CWE-772):
trace-cmd-v3.2/lib/trace-cmd/trace-output.c:1100: open_fn: Returning handle opened by "open". [Note: The source code implementation of the function has been overridden by a user model.]
trace-cmd-v3.2/lib/trace-cmd/trace-output.c:1100: var_assign: Assigning: "fd" = handle returned from "open(path, 0)".
trace-cmd-v3.2/lib/trace-cmd/trace-output.c:1107: noescape: Resource "fd" is not freed or pointed-to in "read". [Note: The source code implementation of the function has been overridden by a builtin model.]
trace-cmd-v3.2/lib/trace-cmd/trace-output.c:1120: off_by_one: Testing whether handle "fd" is strictly greater than zero is suspicious.  "fd" leaks when it is zero.
trace-cmd-v3.2/lib/trace-cmd/trace-output.c:1120: remediation: Did you intend to include equality with zero?
trace-cmd-v3.2/lib/trace-cmd/trace-output.c:1124: leaked_handle: Handle variable "fd" going out of scope leaks the handle.
# 1122|   	if (ret)
# 1123|   		tracecmd_warning("can''t set kptr_restrict");
# 1124|-> }
# 1125|   
# 1126|   static int read_proc_kallsyms(struct tracecmd_output *handle, bool compress)', true, 'Non-Issue', 'open could only return zero if we close stdin, which we don''t.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.218934');
INSERT INTO public.ground_truth VALUES (536, 'trace-cmd-3.2-2.el10', 48, 'Error: USE_AFTER_FREE (CWE-416):
trace-cmd-v3.2/lib/trace-cmd/trace-util.c:150: freed_arg: "free" frees "comm".
trace-cmd-v3.2/lib/trace-cmd/trace-util.c:149: pass_freed_arg: Passing freed pointer "comm" as an argument to "tep_register_comm".
#  147|   	while (line) {
#  148|   		sscanf(line, "%d %m[^\n]s", &pid, &comm);
#  149|-> 		tep_register_comm(pevent, comm, pid);
#  150|   		free(comm);
#  151|   		line = strtok_r(NULL, "\n", &next);', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-40111

Actually not a use-after-free issue, since the function isn''t used at all. But it would have been if it was.', 'Sequential execution of lines 149 and 150 introduces a potential Use After Free vulnerability, as `tep_register_comm` may store the `comm` pointer for later use after it''s freed, with no explicit evidence in the provided code that a copy is created or stored safely.', '2025-11-18 16:18:56.218934');
INSERT INTO public.ground_truth VALUES (537, 'trace-cmd-3.2-2.el10', 49, 'Error: RESOURCE_LEAK (CWE-772):
trace-cmd-v3.2/tracecmd/trace-record.c:3759: alloc_fn: Storage is returned from allocation function "tracecmd_msg_handle_alloc".
trace-cmd-v3.2/tracecmd/trace-record.c:3759: var_assign: Assigning: "msg_handle" = storage returned from "tracecmd_msg_handle_alloc(sfd, 0UL)".
trace-cmd-v3.2/tracecmd/trace-record.c:3779: noescape: Resource "msg_handle" is not freed or pointed-to in "check_protocol_version".
trace-cmd-v3.2/tracecmd/trace-record.c:3753: leaked_storage: Variable "msg_handle" going out of scope leaks the storage it points to.
# 3751|   	if (sfd < 0) {
# 3752|   		free(thost);
# 3753|-> 		return NULL;
# 3754|   	}
# 3755|', true, 'Non-Issue', 'The program exits right away if  setup_network() return NULL. I fix it upstream anyway', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.218934');
INSERT INTO public.ground_truth VALUES (538, 'trace-cmd-3.2-2.el10', 50, 'Error: INTEGER_OVERFLOW (CWE-190):
trace-cmd-v3.2/lib/trace-cmd/trace-input.c:362: tainted_data_return: Called function "read(fd, data + tot, size - tot)", and a possible return value may be less than zero.
trace-cmd-v3.2/lib/trace-cmd/trace-input.c:362: assign: Assigning: "r" = "read(fd, data + tot, size - tot)".
trace-cmd-v3.2/lib/trace-cmd/trace-input.c:363: overflow: The expression "tot" is considered to have possibly overflowed.
trace-cmd-v3.2/lib/trace-cmd/trace-input.c:362: overflow: The expression "size - tot" is deemed overflowed because at least one of its arguments has overflowed.
trace-cmd-v3.2/lib/trace-cmd/trace-input.c:362: overflow_sink: "size - tot", which might have underflowed, is passed to "read(fd, data + tot, size - tot)". [Note: The source code implementation of the function has been overridden by a builtin model.]
#  360|   
#  361|   	do {
#  362|-> 		r = read(fd, data + tot, size - tot);
#  363|   		tot += r;
#  364|', true, 'Non-Issue', 'If ret < 0, we function returns right away and the possible overflowed value discarded.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.218934');
INSERT INTO public.ground_truth VALUES (539, 'trace-cmd-3.2-2.el10', 51, 'Error: BUFFER_SIZE (CWE-170):
trace-cmd-v3.2/tracecmd/trace-setup-guest.c:24: buffer_size_warning: Calling "strncpy" with a maximum size argument of 4098 bytes on destination array "buf" of size 4098 bytes might leave the destination string unterminated.
#   22|   	char buf[PATH_MAX+2], *p;
#   23|   
#   24|-> 	strncpy(buf, path, sizeof(buf));
#   25|   	if (buf[PATH_MAX])
#   26|   		return -E2BIG;', true, 'Non-Issue', 'The code check that buf is null terminated in the next line.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.218934');
INSERT INTO public.ground_truth VALUES (540, 'trace-cmd-3.2-2.el10', 52, 'Error: RESOURCE_LEAK (CWE-772):
trace-cmd-v3.2/tracecmd/trace-listen.c:452: alloc_fn: Storage is returned from allocation function "malloc".
trace-cmd-v3.2/tracecmd/trace-listen.c:452: var_assign: Assigning: "option" = storage returned from "malloc(size)".
trace-cmd-v3.2/tracecmd/trace-listen.c:460: noescape: Resource "option + s" is not freed or pointed-to in "read". [Note: The source code implementation of the function has been overridden by a builtin model.]
trace-cmd-v3.2/tracecmd/trace-listen.c:483: leaked_storage: Variable "option" going out of scope leaks the storage it points to.
#  481|   	free(last_proto);
#  482|   
#  483|-> 	return ret;
#  484|   }
#  485|', false, 'Non-Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-40111

The loop is wrong too. It could loop indefinitely if the option isn''t read in one call of read()', 'Variable ''option'' is explicitly freed at line 468 (`free(option);`) before going out of scope at line 483, contradicting the ''leaked_storage'' claim, and all allocated memory (including ''option + s'') is covered by this free operation.', '2025-11-18 16:18:56.218934');
INSERT INTO public.ground_truth VALUES (541, 'trace-cmd-3.2-2.el10', 53, 'Error: RESOURCE_LEAK (CWE-772):
trace-cmd-v3.2/lib/trace-cmd/trace-hooks.c:29: alloc_fn: Storage is returned from allocation function "malloc".
trace-cmd-v3.2/lib/trace-cmd/trace-hooks.c:29: var_assign: Assigning: "hook" = storage returned from "malloc(104UL)".
trace-cmd-v3.2/lib/trace-cmd/trace-hooks.c:32: noescape: Resource "hook" is not freed or pointed-to in "memset". [Note: The source code implementation of the function has been overridden by a builtin model.]
trace-cmd-v3.2/lib/trace-cmd/trace-hooks.c:154: leaked_storage: Variable "hook" going out of scope leaks the storage it points to.
#  152|   invalid_tok:
#  153|   	tracecmd_warning("Invalid hook format ''%s''", arg);
#  154|-> 	return NULL;
#  155|   }
#  156|', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-40111

', 'Memory allocated for `hook` via `malloc` (line 29) is not freed in the `invalid_tok` error path (line 154), leading to a memory leak upon error exit, with no implicit memory management mechanisms in the provided code snippet to mitigate this issue.', '2025-11-18 16:18:56.218934');
INSERT INTO public.ground_truth VALUES (542, 'trace-cmd-3.2-2.el10', 54, 'Error: RESOURCE_LEAK (CWE-772):
trace-cmd-v3.2/tracecmd/trace-show.c:67: alloc_fn: Storage is returned from allocation function "allocate_instance".
trace-cmd-v3.2/tracecmd/trace-show.c:67: var_assign: Assigning: "instance" = storage returned from "allocate_instance(optarg)".
trace-cmd-v3.2/tracecmd/trace-show.c:67: overwrite_var: Overwriting "instance" in "instance = allocate_instance(optarg)" leaks the storage that "instance" points to.
#   65|   				die("Can only show one buffer at a time");
#   66|   			buffer = optarg;
#   67|-> 			instance = allocate_instance(optarg);
#   68|   			if (!instance)
#   69|   				die("Failed to create instance");', true, 'Non-Issue', 'We can set only one -B option.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.218934');
INSERT INTO public.ground_truth VALUES (543, 'trace-cmd-3.2-2.el10', 55, 'Error: UNINIT (CWE-457):
trace-cmd-v3.2/python/ctracecmd_wrap.c:18170: var_decl: Declaring variable "temp3" without initializer.
trace-cmd-v3.2/python/ctracecmd_wrap.c:18175: assign: Assigning: "arg3" = "&temp3", which points to uninitialized data.
trace-cmd-v3.2/python/ctracecmd_wrap.c:18188: uninit_use_in_call: Using uninitialized value "*arg3" when calling "tep_plugin_kvm_get_func".
#18186|     }
#18187|     arg2 = (struct tep_record *)(argp2);
#18188|->   result = (char *)tep_plugin_kvm_get_func(arg1,arg2,arg3);
#18189|     resultobj = SWIG_FromCharPtr((const char *)result);
#18190|     if (SWIG_IsTmpObj(res3)) {', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-40111

This is related to the genaration of python wrapper with swig. I have no previous experience with swig, but the configuration looks wrong to me. I''m going to need help from upstream.', 'Variable `temp3` is declared without initialization (line 18170), its address is assigned to `arg3` (line 18175), and its uninitialized value is used in `tep_plugin_kvm_get_func` (line 18188), directly correlating with CWE-457 (Uninitialized Variable) with no explicit initialization in the provided code context.', '2025-11-18 16:18:56.218934');
INSERT INTO public.ground_truth VALUES (544, 'trace-cmd-3.2-2.el10', 56, 'Error: INTEGER_OVERFLOW (CWE-190):
trace-cmd-v3.2/lib/trace-cmd/trace-msg.c:346: tainted_data_return: Called function "read(fd, buf + *n, size)", and a possible return value may be less than zero.
trace-cmd-v3.2/lib/trace-cmd/trace-msg.c:346: assign: Assigning: "r" = "read(fd, buf + *n, size)".
trace-cmd-v3.2/lib/trace-cmd/trace-msg.c:353: overflow: The expression "size -= r" might be negative, but is used in a context that treats it as unsigned.
trace-cmd-v3.2/lib/trace-cmd/trace-msg.c:346: overflow_sink: "size", which might be negative, is passed to "read(fd, buf + *n, size)". [Note: The source code implementation of the function has been overridden by a builtin model.]
#  344|   
#  345|   	while (size) {
#  346|-> 		r = read(fd, buf + *n, size);
#  347|   		if (r < 0) {
#  348|   			if (errno == EINTR)', true, 'Non-Issue', 'We explicitely check for r < 0 and it size -= r isn''t executed in this case.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.218934');
INSERT INTO public.ground_truth VALUES (545, 'trace-cmd-3.2-2.el10', 57, 'Error: INTEGER_OVERFLOW (CWE-190):
trace-cmd-v3.2/lib/trace-cmd/trace-compress.c:68: tainted_data_return: Called function "write(fd, data + tot, size - tot)", and a possible return value may be less than zero.
trace-cmd-v3.2/lib/trace-cmd/trace-compress.c:68: cast_overflow: An assign that casts to a different type, which might trigger an overflow.
trace-cmd-v3.2/lib/trace-cmd/trace-compress.c:69: overflow: The expression "tot" is considered to have possibly overflowed.
trace-cmd-v3.2/lib/trace-cmd/trace-compress.c:68: overflow: The expression "size - tot" is deemed overflowed because at least one of its arguments has overflowed.
trace-cmd-v3.2/lib/trace-cmd/trace-compress.c:68: overflow_sink: "size - tot", which might have underflowed, is passed to "write(fd, data + tot, size - tot)".
#   66|   
#   67|   	do {
#   68|-> 		w = write(fd, data + tot, size - tot);
#   69|   		tot += w;
#   70|', true, 'Non-Issue', NULL, 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.218934');
INSERT INTO public.ground_truth VALUES (546, 'trace-cmd-3.2-2.el10', 58, 'Error: RESOURCE_LEAK (CWE-772):
trace-cmd-v3.2/tracecmd/trace-listen.c:698: open_fn: Returning handle opened by "create_client_file".
trace-cmd-v3.2/tracecmd/trace-listen.c:698: var_assign: Assigning: "ofd" = handle returned from "create_client_file(node, port)".
trace-cmd-v3.2/tracecmd/trace-listen.c:709: noescape: Resource "ofd" is not freed or pointed-to in "tracecmd_msg_collect_data".
trace-cmd-v3.2/tracecmd/trace-listen.c:731: leaked_handle: Handle variable "ofd" going out of scope leaks the handle.
#  729|   	destroy_all_readers(cpus, pid_array, node, port);
#  730|   
#  731|-> 	return ret;
#  732|   }
#  733|', false, 'Non-Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-40111

', 'Handle ''ofd'' is potentially closed within `tracecmd_msg_collect_data` (line 709) via `tracecmd_msg_wait_close`, and is utilized beyond the reported leak point (lines 726), suggesting no explicit evidence for a leak within the provided code snippets.', '2025-11-18 16:18:56.218934');
INSERT INTO public.ground_truth VALUES (547, 'trace-cmd-3.2-2.el10', 59, 'Error: INTEGER_OVERFLOW (CWE-190):
trace-cmd-v3.2/lib/trace-cmd/trace-msg.c:992: tainted_data_return: Called function "write(ofd, msg.buf + s, t)", and a possible return value may be less than zero.
trace-cmd-v3.2/lib/trace-cmd/trace-msg.c:992: assign: Assigning: "s" = "write(ofd, msg.buf + s, t)".
trace-cmd-v3.2/lib/trace-cmd/trace-msg.c:1000: overflow: The expression "t" is considered to have possibly overflowed.
trace-cmd-v3.2/lib/trace-cmd/trace-msg.c:992: overflow_sink: "t", which might have overflowed, is passed to "write(ofd, msg.buf + s, t)".
#  990|   		s = 0;
#  991|   		while (t > 0) {
#  992|-> 			s = write(ofd, msg.buf+s, t);
#  993|   			if (s < 0) {
#  994|   				if (errno == EINTR)', true, 'Non-Issue', 'If s < 0, we either skip the problematic asignment or exit the function altogether. ', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.218934');
INSERT INTO public.ground_truth VALUES (472, 'texinfo-7.1-2.el10', 27, 'Error: OVERRUN (CWE-119):
texinfo-7.1/info/session.c:3558: alloc_strlen: Allocating insufficient memory for the terminating null of the string. [Note: The source code implementation of the function has been overridden by a builtin model.]
# 3556|     default_program_name = program_name_from_file_name (file_name);
# 3557|   
# 3558|->   prompt = xmalloc (strlen (default_program_name) +
# 3559|   		    strlen (invocation_prompt));
# 3560|     sprintf (prompt, invocation_prompt, default_program_name);', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-43595

', NULL, '2025-11-18 16:18:56.203498');
INSERT INTO public.ground_truth VALUES (549, 'trace-cmd-3.2-2.el10', 61, 'Error: RESOURCE_LEAK (CWE-772):
trace-cmd-v3.2/tracecmd/trace-mem.c:552: alloc_fn: Storage is returned from allocation function "tracecmd_alloc".
trace-cmd-v3.2/tracecmd/trace-mem.c:552: var_assign: Assigning: "handle" = storage returned from "tracecmd_alloc(input_file, 0)".
trace-cmd-v3.2/tracecmd/trace-mem.c:556: noescape: Resource "handle" is not freed or pointed-to in "tracecmd_read_headers".
trace-cmd-v3.2/tracecmd/trace-mem.c:558: leaked_storage: Variable "handle" going out of scope leaks the storage it points to.
#  556|   	ret = tracecmd_read_headers(handle, 0);
#  557|   	if (ret)
#  558|-> 		return;
#  559|   
#  560|   	do_trace_mem(handle);', false, 'Non-Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-40111

', 'The reported memory leak at line 558 is conditional upon an error, and while the provided code doesn''t explicitly show cleanup on error, the successful execution path (ret == 0) properly closes the resource at line 562, leading to a FALSE POSITIVE conclusion based on explicit code behavior.', '2025-11-18 16:18:56.218934');
INSERT INTO public.ground_truth VALUES (550, 'trace-cmd-3.2-2.el10', 62, 'Error: UNINIT (CWE-457):
trace-cmd-v3.2/tracecmd/trace-vm.c:271: var_decl: Declaring variable "trace_fields" without initializer.
trace-cmd-v3.2/tracecmd/trace-vm.c:307: uninit_use: Using uninitialized value "trace_fields.found_pid".
#  305|           tracefs_instance_free(open_instance);
#  306|   
#  307|->         if (trace_fields.found_pid > 0)
#  308|                   tgid = find_tgid(trace_fields.found_pid);
#  309|', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-40111

', 'Despite initial concerns, `trace_fields.found_pid` is explicitly initialized to `-1` on line 298, preceding its use on line 307 with no intervening conditionals that could bypass this initialization, mitigating the uninitialized use vulnerability.', '2025-11-18 16:18:56.218934');
INSERT INTO public.ground_truth VALUES (551, 'trace-cmd-3.2-2.el10', 63, 'Error: UNINIT (CWE-457):
trace-cmd-v3.2/tracecmd/trace-profile.c:1624: var_decl: Declaring variable "ts_min" without initializer.
trace-cmd-v3.2/tracecmd/trace-profile.c:1707: uninit_use: Using uninitialized value "ts_min".
# 1705|   			chain[x].time = time;
# 1706|   			chain[x].time_min = time_min;
# 1707|-> 			chain[x].ts_min = ts_min;
# 1708|   			chain[x].time_max = time_max;
# 1709|   			chain[x].ts_max = ts_max;', true, 'Non-Issue', 'The previous loop checks that there are values to be read and thus ts_min and ts_max has been set i reaches cnt-1', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.218934');
INSERT INTO public.ground_truth VALUES (552, 'trace-cmd-3.2-2.el10', 64, 'Error: OVERRUN (CWE-119):
trace-cmd-v3.2/tracecmd/trace-record.c:3653: identity_transfer: Passing "8192UL" as argument 3 to function "read", which returns that argument. [Note: The source code implementation of the function has been overridden by a builtin model.]
trace-cmd-v3.2/tracecmd/trace-record.c:3653: assignment: Assigning: "n" = "read(fd, buf, 8192UL)". The value of "n" is now 8192.
trace-cmd-v3.2/tracecmd/trace-record.c:3660: overrun-buffer-arg: Overrunning array ""V3"" of 3 bytes by passing it to a function which accesses it at byte offset 8191 using argument "n" (which evaluates to 8192).
# 3658|                   tracecmd_plog("Use the v1 protocol\n");
# 3659|           } else {
# 3660|->                 if (memcmp(buf, "V3", n) != 0)
# 3661|                           die("Cannot handle the protocol %s", buf);
# 3662|                   /* OK, let''s use v3 protocol */', false, 'Non-Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-40111

', 'The reported buffer overrun is deemed FALSE POSITIVE since `buf` is sized to match the maximum read length (`BUFSIZ`/8192), ensuring no overflow occurs when `n` equals 8192, and the vulnerable `memcmp` call''s issue is a misuse of `n` with a short string, not an overrun of the 8192-byte `buf` array.', '2025-11-18 16:18:56.218934');
INSERT INTO public.ground_truth VALUES (553, 'trace-cmd-3.2-2.el10', 65, 'Error: RESOURCE_LEAK (CWE-772):
trace-cmd-v3.2/tracecmd/trace-record.c:3496: open_fn: Returning handle opened by "do_accept".
trace-cmd-v3.2/tracecmd/trace-record.c:3496: var_assign: Assigning: "fd" = handle returned from "do_accept(instance->fds[cpu])".
trace-cmd-v3.2/tracecmd/trace-record.c:3497: noescape: Resource "fd" is not freed or pointed-to in "trace_net_cmp_connection_fd".
trace-cmd-v3.2/tracecmd/trace-record.c:3496: overwrite_var: Overwriting handle "fd" in "fd = do_accept(instance->fds[cpu])" leaks the handle.
# 3494|   			else {
# 3495|    again:
# 3496|-> 				fd = do_accept(instance->fds[cpu]);
# 3497|   				if (instance->host &&
# 3498|   				    !trace_net_cmp_connection_fd(fd, instance->host)) {', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-40111

', 'Variable `fd` is overwritten with a new handle on line 3496 without explicit freeing of the previous value, potentially leading to a handle leak, exacerbated by the `again` loop (line 3495), with no provided code showing explicit closure or freeing of the `fd` handle outside this scope.', '2025-11-18 16:18:56.218934');
INSERT INTO public.ground_truth VALUES (554, 'trace-cmd-3.2-2.el10', 66, 'Error: RESOURCE_LEAK (CWE-772):
trace-cmd-v3.2/lib/trace-cmd/trace-timesync.c:288: alloc_fn: Storage is returned from allocation function "calloc".
trace-cmd-v3.2/lib/trace-cmd/trace-timesync.c:288: var_assign: Assigning: "plist" = storage returned from "calloc(1UL, 8UL)".
trace-cmd-v3.2/lib/trace-cmd/trace-timesync.c:293: leaked_storage: Variable "plist" going out of scope leaks the storage it points to.
#  291|   	plist->names = calloc(count, sizeof(char *));
#  292|   	if (!plist->names)
#  293|-> 		return -1;
#  294|   
#  295|   	for (i = 0, proto = tsync_proto_list; proto && i < (count - 1); proto = proto->next) {', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-40111

', 'Memory allocated for `plist` at line 288 is not freed before returning at line 293 when `plist->names` allocation fails, creating a clear execution path for a memory leak.', '2025-11-18 16:18:56.218934');
INSERT INTO public.ground_truth VALUES (555, 'trace-cmd-3.2-2.el10', 67, 'Error: UNINIT (CWE-457):
trace-cmd-v3.2/tracecmd/trace-profile.c:1626: var_decl: Declaring variable "ts_max" without initializer.
trace-cmd-v3.2/tracecmd/trace-profile.c:1709: uninit_use: Using uninitialized value "ts_max".
# 1707|   			chain[x].ts_min = ts_min;
# 1708|   			chain[x].time_max = time_max;
# 1709|-> 			chain[x].ts_max = ts_max;
# 1710|   			chain[x].children =
# 1711|   				make_stack_chain(&stacks[start], (i - start) + 1,', true, 'Non-Issue', 'The previous loop checks that there are values to be read and thus ts_min and ts_max has been set i reaches cnt-1', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.218934');
INSERT INTO public.ground_truth VALUES (556, 'trace-cmd-3.2-2.el10', 68, 'Error: RESOURCE_LEAK (CWE-772):
trace-cmd-v3.2/tracecmd/trace-show.c:67: alloc_fn: Storage is returned from allocation function "allocate_instance".
trace-cmd-v3.2/tracecmd/trace-show.c:67: var_assign: Assigning: "instance" = storage returned from "allocate_instance(optarg)".
trace-cmd-v3.2/tracecmd/trace-show.c:171: leaked_storage: Variable "instance" going out of scope leaks the storage it points to.
#  169|   		free(path);
#  170|   
#  171|-> 	return;
#  172|   }', true, 'Non-Issue', 'The program exit when trace_show returns.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.218934');
INSERT INTO public.ground_truth VALUES (580, 'util-linux-2.40-0.8.rc1.el10', 1, 'Error: INTEGER_OVERFLOW (CWE-190):
util-linux-2.40-rc1/term-utils/ttymsg.c:129: underflow: The decrement operator on the unsigned variable "iovcnt" might result in an underflow.
util-linux-2.40-rc1/term-utils/ttymsg.c:116: overflow_sink: "iovcnt", which might have underflowed, is passed to "writev(fd, iov, iovcnt)".
#  114|   
#  115|   	for (;;) {
#  116|-> 		wret = writev(fd, iov, iovcnt);
#  117|   		if (wret >= (ssize_t) left)
#  118|   			break;', false, 'Non-Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-38423

fixed in v2.40.1', 'iovcnt, an unsigned size_t, cannot underflow in a security-vulnerable manner due to its type and the loop''s conditional checks (lines 71, 117, 129), preventing reachable underflow scenarios that could exploit the system.', '2025-11-18 16:18:56.249001');
INSERT INTO public.ground_truth VALUES (581, 'util-linux-2.40-0.8.rc1.el10', 2, 'Error: UNINIT (CWE-457):
util-linux-2.40-rc1/libfdisk/src/dos.c:793: var_decl: Declaring variable "t" without initializer.
util-linux-2.40-rc1/libfdisk/src/dos.c:862: uninit_use: Using uninitialized value "t[i].c".
#  860|   			n1 = t[i].o * t[j].h;
#  861|   			n2 = t[j].o * t[i].h;
#  862|-> 			n3 = t[j].o * t[i].c;
#  863|   			n4 = t[i].o * t[j].c;
#  864|   			n5 = t[i].c * t[j].h;', false, 'Non-Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-38423

fixed in v2.40.1', 'Elements of ''t'' are explicitly initialized via the ''chs_set_t'' macro (lines 801, 842, 852) before their use (line 862), with all elements initialized in the loop (lines 832-853) prior to the reported vulnerable nested loop (line 855).', '2025-11-18 16:18:56.249001');
INSERT INTO public.ground_truth VALUES (582, 'util-linux-2.40-0.8.rc1.el10', 3, 'Error: UNINIT (CWE-457):
util-linux-2.40-rc1/libfdisk/src/dos.c:793: var_decl: Declaring variable "t" without initializer.
util-linux-2.40-rc1/libfdisk/src/dos.c:860: uninit_use: Using uninitialized value "t[i].o".
#  858|   			if (!t[i].v || !t[j].v)
#  859|   				continue;
#  860|-> 			n1 = t[i].o * t[j].h;
#  861|   			n2 = t[j].o * t[i].h;
#  862|   			n3 = t[j].o * t[i].c;', false, 'Non-Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-38423

fixed in v2.40.1', 'Variable ''t'' is initialized through the ''chs_set_t'' macro (lines 801-805) before its elements, including ''t[i].o'', are used at line 860, with additional safeguards from validity checks (line 858) ensuring initialized access.', '2025-11-18 16:18:56.249001');
INSERT INTO public.ground_truth VALUES (583, 'util-linux-2.40-0.8.rc1.el10', 4, 'Error: UNINIT (CWE-457):
util-linux-2.40-rc1/libsmartcols/src/filter-parser.c:1173: var_decl: Declaring variable "yylval" without initializer.
util-linux-2.40-rc1/libsmartcols/src/filter-parser.c:1712: uninit_use: Using uninitialized value "yylval".
# 1710|   
# 1711|     YY_IGNORE_MAYBE_UNINITIALIZED_BEGIN
# 1712|->   *++yyvsp = yylval;
# 1713|     YY_IGNORE_MAYBE_UNINITIALIZED_END
# 1714|', true, 'Non-Issue', 'code generated by bison', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.249001');
INSERT INTO public.ground_truth VALUES (584, 'util-linux-2.40-0.8.rc1.el10', 5, 'Error: RESOURCE_LEAK (CWE-772):
util-linux-2.40-rc1/misc-utils/findmnt-verify.c:95: alloc_fn: Storage is returned from allocation function "mnt_resolve_target".
util-linux-2.40-rc1/misc-utils/findmnt-verify.c:95: var_assign: Assigning: "tgt" = storage returned from "mnt_resolve_target(tgt, cache)".
util-linux-2.40-rc1/misc-utils/findmnt-verify.c:132: leaked_storage: Variable "tgt" going out of scope leaks the storage it points to.
#  130|   done:
#  131|   	mnt_free_iter(itr);
#  132|-> 	return 0;
#  133|   }
#  134|', true, 'Non-Issue', 'the allocation is maitained by the cache struct', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.249001');
INSERT INTO public.ground_truth VALUES (585, 'util-linux-2.40-0.8.rc1.el10', 6, 'Error: INTEGER_OVERFLOW (CWE-125):
util-linux-2.40-rc1/term-utils/setterm.c:918: tainted_data_return: Called function "read(0, retstr + pos, 31UL - pos)", and a possible return value may be less than zero.
util-linux-2.40-rc1/term-utils/setterm.c:918: assign: Assigning: "rc" = "read(0, retstr + pos, 31UL - pos)".
util-linux-2.40-rc1/term-utils/setterm.c:927: overflow: The expression "pos" is considered to have possibly overflowed.
util-linux-2.40-rc1/term-utils/setterm.c:928: overflow: The expression "pos - 1UL" is deemed overflowed because at least one of its arguments has overflowed.
util-linux-2.40-rc1/term-utils/setterm.c:928: deref_overflow: "pos - 1UL", which might have underflowed, is passed to "retstr[pos - 1UL]".
#  926|   		}
#  927|   		pos += rc;
#  928|-> 		if (retstr[pos - 1] == ''R'')
#  929|   			break;
#  930|   	}', true, 'Non-Issue', 'all the assigning is in "if < 0"', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.249001');
INSERT INTO public.ground_truth VALUES (333, 'sqlite-3.45.1-2.el10', 2, 'Error: OVERRUN (CWE-119):
sqlite-src-3450100/sqlite3_analyzer.c:35667: assignment: Assigning: "i" = "23".
sqlite-src-3450100/sqlite3_analyzer.c:35677: overrun-local: Overrunning array "p->zBuf" of 24 bytes at byte offset 24 using index "i + 1" (which evaluates to 24).
#35675|     if( iRound<0 ){
#35676|       iRound = p->iDP - iRound;
#35677|->     if( iRound==0 && p->zBuf[i+1]>=''5'' ){
#35678|         iRound = 1;
#35679|         p->zBuf[i--] = ''0'';', true, 'Non-Issue', 'There is while that decrements the i at elast once.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (587, 'util-linux-2.40-0.8.rc1.el10', 8, 'Error: RESOURCE_LEAK (CWE-772):
util-linux-2.40-rc1/misc-utils/lsclocks.c:257: open_fn: Returning handle opened by "open". [Note: The source code implementation of the function has been overridden by a user model.]
util-linux-2.40-rc1/misc-utils/lsclocks.c:257: var_assign: Assigning: "fd" = handle returned from "open("/proc/self/timens_offsets", 0)".
util-linux-2.40-rc1/misc-utils/lsclocks.c:261: noescape: Resource "fd" is not freed or pointed-to in "read_all_alloc".
util-linux-2.40-rc1/misc-utils/lsclocks.c:280: leaked_handle: Handle variable "fd" going out of scope leaks the handle.
#  278|   
#  279|   	free(buf);
#  280|-> 	return ret;
#  281|   }
#  282|', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-38423

fixed in v2.40.1', 'File descriptor `fd`, opened at line 257, is not explicitly closed before going out of scope at line 280, despite being utilized in `read_all_alloc` (line 261), directly correlating with the CWE-772 resource leak vulnerability.', '2025-11-18 16:18:56.249001');
INSERT INTO public.ground_truth VALUES (588, 'util-linux-2.40-0.8.rc1.el10', 9, 'Error: INTEGER_OVERFLOW (CWE-190):
util-linux-2.40-rc1/term-utils/ttymsg.c:129: underflow: The decrement operator on the unsigned variable "iovcnt" might result in an underflow.
util-linux-2.40-rc1/term-utils/ttymsg.c:122: overflow: The expression "iovcnt * 16UL" is deemed underflowed because at least one of its arguments has underflowed.
util-linux-2.40-rc1/term-utils/ttymsg.c:122: overflow_sink: "iovcnt * 16UL", which might have underflowed, is passed to "memmove(localiov, iov, iovcnt * 16UL)". [Note: The source code implementation of the function has been overridden by a builtin model.]
#  120|   			left -= wret;
#  121|   			if (iov != localiov) {
#  122|-> 				memmove(localiov, iov,
#  123|   				    iovcnt * sizeof(struct iovec));
#  124|   				iov = localiov;', false, 'Non-Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-38423

fixed in v2.40.1', 'Decrement of unsigned `iovcnt` (line 129) is conditional on successful writes, preventing harmful underflow, and its use in `memmove` (line 122) is validated against `ARRAY_SIZE(localiov)` (line 80), mitigating overflow concerns.', '2025-11-18 16:18:56.249001');
INSERT INTO public.ground_truth VALUES (589, 'util-linux-2.40-0.8.rc1.el10', 10, 'Error: UNINIT (CWE-457):
util-linux-2.40-rc1/sys-utils/hwclock-parse-date.y:1268: var_decl: Declaring variable "tm" without initializer.
util-linux-2.40-rc1/sys-utils/hwclock-parse-date.y:1478: uninit_use: Using uninitialized value "tm". Field "tm.tm_wday" is uninitialized.
# 1476|   			tm.tm_isdst = pc.local_isdst;
# 1477|   
# 1478|-> 		tm0 = tm;
# 1479|   
# 1480|   		Start = mktime (&tm);', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-38423

fixed in v2.40.1', 'Unchanged default value indicates a failure in value replacement, confirming the issue''s validity.', '2025-11-18 16:18:56.249001');
INSERT INTO public.ground_truth VALUES (590, 'util-linux-2.40-0.8.rc1.el10', 11, 'Error: RESOURCE_LEAK (CWE-772):
util-linux-2.40-rc1/misc-utils/findmnt-verify.c:118: alloc_fn: Storage is returned from allocation function "mnt_resolve_target".
util-linux-2.40-rc1/misc-utils/findmnt-verify.c:118: var_assign: Assigning: "n_tgt" = storage returned from "mnt_resolve_target(n_tgt, cache)".
util-linux-2.40-rc1/misc-utils/findmnt-verify.c:121: noescape: Resource "n_tgt" is not freed or pointed-to in "strlen".
util-linux-2.40-rc1/misc-utils/findmnt-verify.c:123: noescape: Resource "n_tgt" is not freed or pointed-to in "strncmp".
util-linux-2.40-rc1/misc-utils/findmnt-verify.c:129: leaked_storage: Variable "n_tgt" going out of scope leaks the storage it points to.
#  127|   				verify_err(vfy, _("wrong order: %s specified before %s"), tgt, n_tgt);
#  128|   		}
#  129|-> 	}
#  130|   done:
#  131|   	mnt_free_iter(itr);', true, 'Non-Issue', 'the allocation is maitained by the cache struct', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.249001');
INSERT INTO public.ground_truth VALUES (591, 'util-linux-2.40-0.8.rc1.el10', 12, 'Error: RESOURCE_LEAK (CWE-772):
util-linux-2.40-rc1/term-utils/wall.c:330: alloc_fn: Storage is returned from allocation function "xgetlogin".
util-linux-2.40-rc1/term-utils/wall.c:330: var_assign: Assigning: "whom" = storage returned from "xgetlogin()".
util-linux-2.40-rc1/term-utils/wall.c:356: noescape: Resource "whom" is not freed or pointed-to in "snprintf". [Note: The source code implementation of the function has been overridden by a builtin model.]
util-linux-2.40-rc1/term-utils/wall.c:361: leaked_storage: Variable "whom" going out of scope leaks the storage it points to.
#  359|   		fprintf(fs, "%-*.*s\007\007\r\n", TERM_WIDTH, TERM_WIDTH, lbuf);
#  360|   		free(hostname);
#  361|-> 	}
#  362|   	fprintf(fs, "%*s\r\n", TERM_WIDTH, " ");
#  363|', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-38423

fixed in v2.40.1', 'Variable `whom` is allocated memory via `xgetlogin()` at line 330, used indirectly in `snprintf()` at line 356, and then goes out of scope without being freed at line 361, indicating a memory leak with no mitigating context in the provided code.', '2025-11-18 16:18:56.249001');
INSERT INTO public.ground_truth VALUES (592, 'util-linux-2.40-0.8.rc1.el10', 13, 'Error: OVERRUN (CWE-119):
util-linux-2.40-rc1/misc-utils/cal.c:367: assignment: Assigning: "wfd" = "val.word".
util-linux-2.40-rc1/misc-utils/cal.c:368: overrun-call: Overrunning callee''s array of size 12 by passing argument "wfd / 100 % 100" (which evaluates to 99) in call to "day_in_week".
#  366|   
#  367|   		wfd = val.word;
#  368|-> 		wfd = day_in_week(&ctl, wfd % 100, (wfd / 100) % 100,
#  369|   				  wfd / (100 * 100));
#  370|   		ctl.weekstart = (wfd + *nl_langinfo(_NL_TIME_FIRST_WEEKDAY) - 1) % DAYS_IN_WEEK;', false, 'Non-Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-38423

fixed in v2.40.1', 'day_in_week function takes int parameters, not an array, and ''wfd / 100 % 100'' (evaluating to 99) is a valid input for the month parameter, not an array index, thus no array overrun occurs (lines 1086-1127, shared-data/source/util-linux-2.40-rc1/misc-utils/cal.c).', '2025-11-18 16:18:56.249001');
INSERT INTO public.ground_truth VALUES (593, 'util-linux-2.40-0.8.rc1.el10', 14, 'Error: INTEGER_OVERFLOW (CWE-190):
util-linux-2.40-rc1/term-utils/ttymsg.c:126: tainted_data_argument: The check "wret >= (ssize_t)iov->iov_len" contains the tainted expression "wret" which causes "iov->iov_len" to be considered tainted.
util-linux-2.40-rc1/term-utils/ttymsg.c:133: overflow: The expression "iov->iov_len -= wret" is deemed underflowed because at least one of its arguments has underflowed.
util-linux-2.40-rc1/term-utils/ttymsg.c:116: overflow_sink: "iov->iov_len", which might have underflowed, is passed to "writev(fd, iov, iovcnt)".
#  114|   
#  115|   	for (;;) {
#  116|-> 		wret = writev(fd, iov, iovcnt);
#  117|   		if (wret >= (ssize_t) left)
#  118|   			break;', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-38423

fixed in v2.40.1', 'Potential underflow in `iov->iov_len -= wret` (line 133) due to tainted `wret` (line 126), lacking explicit bounds checking, and subsequently passed to `writev()` (line 116), posing a risk of data corruption or information disclosure.', '2025-11-18 16:18:56.249001');
INSERT INTO public.ground_truth VALUES (594, 'util-linux-2.40-0.8.rc1.el10', 15, 'Error: RESOURCE_LEAK (CWE-772):
util-linux-2.40-rc1/sys-utils/lsns.c:518: alloc_fn: Storage is returned from allocation function "xcalloc".
util-linux-2.40-rc1/sys-utils/lsns.c:518: var_assign: Assigning: "p" = storage returned from "xcalloc(1UL, 384UL)".
util-linux-2.40-rc1/sys-utils/lsns.c:565: leaked_storage: Variable "p" going out of scope leaks the storage it points to.
#  563|   	if (rc)
#  564|   		free(p);
#  565|-> 	return rc;
#  566|   }
#  567|', true, 'Non-Issue', 'variable added to list', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.249001');
INSERT INTO public.ground_truth VALUES (596, 'util-linux-2.40-0.8.rc1.el10', 17, 'Error: RESOURCE_LEAK (CWE-772):
util-linux-2.40-rc1/misc-utils/uuidd.c:272: open_fn: Returning handle opened by "socket".
util-linux-2.40-rc1/misc-utils/uuidd.c:272: var_assign: Assigning: "s" = handle returned from "socket(1, SOCK_STREAM, 0)".
util-linux-2.40-rc1/misc-utils/uuidd.c:283: noescape: Resource "s" is not freed or pointed-to in "dup".
util-linux-2.40-rc1/misc-utils/uuidd.c:283: overwrite_var: Overwriting handle "s" in "s = dup(s)" leaks the handle.
#  281|   	 */
#  282|   	while (will_fork && s <= 2) {
#  283|-> 		s = dup(s);
#  284|   		if (s < 0)
#  285|   			err(EXIT_FAILURE, "dup");', true, 'Non-Issue', 'expected and wanted', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.249001');
INSERT INTO public.ground_truth VALUES (597, 'util-linux-2.40-0.8.rc1.el10', 18, 'Error: COPY_PASTE_ERROR (CWE-398):
util-linux-2.40-rc1/libfdisk/src/table.c:781: original: "*res = pa" looks like the original copy.
util-linux-2.40-rc1/libfdisk/src/table.c:793: copy_paste_error: "pa" in "*res = pa" looks like a copy-paste error.
util-linux-2.40-rc1/libfdisk/src/table.c:793: remediation: Should it say "pb" instead?
#  791|   		DBG(TAB, ul_debugobj(a, " #%zu UNCHANGED", pb->partno));
#  792|   		*change = FDISK_DIFF_UNCHANGED;
#  793|-> 		*res = pa;
#  794|   	}
#  795|   	return 0;', true, 'Non-Issue', 'expected and wanted', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.249001');
INSERT INTO public.ground_truth VALUES (598, 'util-linux-2.40-0.8.rc1.el10', 19, 'Error: RESOURCE_LEAK (CWE-772):
util-linux-2.40-rc1/misc-utils/findmnt-verify.c:144: alloc_fn: Storage is returned from allocation function "mnt_resolve_target".
util-linux-2.40-rc1/misc-utils/findmnt-verify.c:144: var_assign: Assigning: "cn" = storage returned from "mnt_resolve_target(tgt, cache)".
util-linux-2.40-rc1/misc-utils/findmnt-verify.c:147: noescape: Resource "cn" is not freed or pointed-to in "strcmp".
util-linux-2.40-rc1/misc-utils/findmnt-verify.c:148: noescape: Resource "cn" is not freed or pointed-to in "verify_warn".
util-linux-2.40-rc1/misc-utils/findmnt-verify.c:149: var_assign: Assigning: "tgt" = "cn".
util-linux-2.40-rc1/misc-utils/findmnt-verify.c:150: leaked_storage: Variable "cn" going out of scope leaks the storage it points to.
util-linux-2.40-rc1/misc-utils/findmnt-verify.c:151: noescape: Resource "tgt" is not freed or pointed-to in "stat".
util-linux-2.40-rc1/misc-utils/findmnt-verify.c:163: leaked_storage: Variable "tgt" going out of scope leaks the storage it points to.
#  161|   		verify_ok(vfy, _("target exists"));
#  162|   
#  163|-> 	return 0;
#  164|   }
#  165|', true, 'Non-Issue', 'the allocation is maitained by the cache struct', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.249001');
INSERT INTO public.ground_truth VALUES (599, 'util-linux-2.40-0.8.rc1.el10', 20, 'Error: UNINIT (CWE-457):
util-linux-2.40-rc1/sys-utils/hwclock-parse-date.c:1335: var_decl: Declaring variable "yylval" without initializer.
util-linux-2.40-rc1/sys-utils/hwclock-parse-date.c:2278: uninit_use: Using uninitialized value "yylval".
# 2276|   
# 2277|     YY_IGNORE_MAYBE_UNINITIALIZED_BEGIN
# 2278|->   *++yyvsp = yylval;
# 2279|     YY_IGNORE_MAYBE_UNINITIALIZED_END
# 2280|', true, 'Non-Issue', 'code generated by bison', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.249001');
INSERT INTO public.ground_truth VALUES (600, 'util-linux-2.40-0.8.rc1.el10', 21, 'Error: USE_AFTER_FREE (CWE-416):
util-linux-2.40-rc1/login-utils/su-common.c:450: freed_arg: "supam_cleanup" frees "su->pamh".
util-linux-2.40-rc1/login-utils/su-common.c:451: pass_freed_arg: Passing freed pointer "su->pamh" as an argument to "pam_strerror".
#  449|   	if (is_pam_failure(rc)) {
#  450|   		supam_cleanup(su, rc);
#  451|-> 		errx(EXIT_FAILURE, _("cannot open session: %s"),
#  452|   		     pam_strerror(su->pamh, rc));
#  453|   	} else', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-38423

fixed in v2.40.1', 'Direct use-after-free vulnerability: `su->pamh` is explicitly freed by `supam_cleanup` (line 450) and immediately passed to `pam_strerror` (line 451) within the same conditional block, with no intervening code to prevent this vulnerable execution path.', '2025-11-18 16:18:56.249001');
INSERT INTO public.ground_truth VALUES (601, 'util-linux-2.40-0.8.rc1.el10', 22, 'Error: INTEGER_OVERFLOW (CWE-190):
util-linux-2.40-rc1/term-utils/setterm.c:918: tainted_data_return: Called function "read(0, retstr + pos, 31UL - pos)", and a possible return value may be less than zero.
util-linux-2.40-rc1/term-utils/setterm.c:918: assign: Assigning: "rc" = "read(0, retstr + pos, 31UL - pos)".
util-linux-2.40-rc1/term-utils/setterm.c:927: overflow: The expression "pos" is considered to have possibly overflowed.
util-linux-2.40-rc1/term-utils/setterm.c:931: deref_overflow: "pos", which might have overflowed, is used in a pointer index in "retstr[pos]".
#  929|   			break;
#  930|   	}
#  931|-> 	retstr[pos] = 0;
#  932|   	tty_restore(&saved_attributes, &saved_fl);
#  933|   	rc = sscanf(retstr, "\033[%d;%dR", &row, &col);', true, 'Non-Issue', 'all the assigning is in "if < 0"', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.249001');
INSERT INTO public.ground_truth VALUES (602, 'util-linux-2.40-0.8.rc1.el10', 23, 'Error: INTEGER_OVERFLOW (CWE-190):
util-linux-2.40-rc1/text-utils/colrm.c:84: tainted_data_return: The value returned by "getwc(stdin)" is considered tainted.
util-linux-2.40-rc1/text-utils/colrm.c:84: assign: Assigning: "c" = "getwc(stdin)".
util-linux-2.40-rc1/text-utils/colrm.c:92: underflow: The cast of "c" to a signed type could result in a negative number.
#   90|   			w = (ct ? ct - 1 : 0) - ct;
#   91|   		else {
#   92|-> 			w = wcwidth(c);
#   93|   			if (w < 0)
#   94|   				w = 0;', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-38423

already fixe din upstream tree', 'The assignment of tainted input from `getwc(stdin)` to `c` (line 84) and its subsequent cast to a signed type in `wcwidth(c)` (line 92) could result in an underflow, with the mitigating check (lines 93-94) only addressing the symptom, not the root cause of the underflow vulnerability.', '2025-11-18 16:18:56.249001');
INSERT INTO public.ground_truth VALUES (603, 'util-linux-2.40-0.8.rc1.el10', 24, 'Error: RESOURCE_LEAK (CWE-772):
util-linux-2.40-rc1/term-utils/agetty.c:3014: alloc_fn: Storage is returned from allocation function "ul_path_opendir".
util-linux-2.40-rc1/term-utils/agetty.c:3014: var_assign: Assigning: "dir" = storage returned from "ul_path_opendir(pc, NULL)".
util-linux-2.40-rc1/term-utils/agetty.c:3020: noescape: Resource "dir" is not freed or pointed-to in "xreaddir".
util-linux-2.40-rc1/term-utils/agetty.c:3020: noescape: Resource "dir" is not freed or pointed-to in "xreaddir".
util-linux-2.40-rc1/term-utils/agetty.c:3029: leaked_storage: Variable "dir" going out of scope leaks the storage it points to.
# 3027|   		}
# 3028|   	}
# 3029|-> }', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-38423

fixed in v2.40.1', 'Resource `dir`, allocated at line 3014 via `ul_path_opendir(pc, NULL)`, is not explicitly freed (e.g., via `closedir(dir)`) before going out of scope at line 3029, leading to leaked storage.', '2025-11-18 16:18:56.249001');
INSERT INTO public.ground_truth VALUES (82, 'cpio-2.15-1.el10', 8, 'Error: UNINIT (CWE-457):
cpio-2.15/src/copyin.c:1196: var_decl: Declaring variable "ascii_header" without initializer.
cpio-2.15/src/copyin.c:1209: uninit_use_in_call: Using uninitialized element of array "ascii_header.c_nlink" when calling "from_ascii".
# 1207|     file_hdr->c_uid = FROM_OCTAL (ascii_header.c_uid);
# 1208|     file_hdr->c_gid = FROM_OCTAL (ascii_header.c_gid);
# 1209|->   file_hdr->c_nlink = FROM_OCTAL (ascii_header.c_nlink);
# 1210|     dev = FROM_OCTAL (ascii_header.c_rdev);
# 1211|     file_hdr->c_rdev_maj = major (dev);', true, 'Non-Issue', 'initialized by the "tape_buffered read"', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.051962');
INSERT INTO public.ground_truth VALUES (604, 'util-linux-2.40-0.8.rc1.el10', 25, 'Error: OVERRUN (CWE-119):
util-linux-2.40-rc1/disk-utils/fsck.minix.c:551: cond_at_most: Checking "size < 1024UL" implies that "size" may be up to 1023 on the true branch.
util-linux-2.40-rc1/disk-utils/fsck.minix.c:552: overrun-local: Overrunning array of 1024 bytes at byte offset 1025 by dereferencing pointer "blk + size + 2".
#  550|   
#  551|   	for (size = 16; size < MINIX_BLOCK_SIZE; size <<= 1) {
#  552|-> 		if (strcmp(blk + size + 2, "..") == 0) {
#  553|   			dirsize = size;
#  554|   			namelen = size - 2;', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-38423

fixed in v2.40.1', 'The loop at line 551 allows `size` to reach up to `MINIX_BLOCK_SIZE - 1` (e.g., 1023), leading to a potential overrun of the 1024-byte `blk` array at `blk + size + 2` (line 552), with no explicit bounds checking to prevent this vulnerability.', '2025-11-18 16:18:56.249001');
INSERT INTO public.ground_truth VALUES (605, 'util-linux-2.40-0.8.rc1.el10', 26, 'Error: CPPCHECK_WARNING (CWE-401):
util-linux-2.40-rc1/misc-utils/getopt.c:450: error[memleak]: Memory leak: ctl.name
#  448|   		case ''T'':
#  449|   			free(ctl.long_options);
#  450|-> 			return TEST_EXIT_CODE;
#  451|   		case ''u'':
#  452|   			ctl.quote = 0;', true, 'Non-Issue', 'free-before-exit is nonsense', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.249001');
INSERT INTO public.ground_truth VALUES (606, 'util-linux-2.40-0.8.rc1.el10', 27, 'Error: UNINIT (CWE-457):
util-linux-2.40-rc1/libsmartcols/src/filter-parser.c:1194: var_decl: Declaring variable "yyvsa" without initializer.
util-linux-2.40-rc1/libsmartcols/src/filter-parser.c:1195: assign: Assigning: "yyvs" = "yyvsa", which points to uninitialized data.
util-linux-2.40-rc1/libsmartcols/src/filter-parser.c:1196: assign: Assigning: "yyvsp" = "yyvs", which points to uninitialized data.
util-linux-2.40-rc1/libsmartcols/src/filter-parser.c:1414: uninit_use: Using uninitialized value "yyvsp[1 - yylen]".
# 1412|        unconditionally makes the parser a bit smaller, and it avoids a
# 1413|        GCC warning that YYVAL may be used uninitialized.  */
# 1414|->   yyval = yyvsp[1-yylen];
# 1415|   
# 1416|', true, 'Non-Issue', 'code generated by bison', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.249001');
INSERT INTO public.ground_truth VALUES (607, 'util-linux-2.40-0.8.rc1.el10', 28, 'Error: OVERRUN (CWE-119):
util-linux-2.40-rc1/lib/sha256.c:90: assignment: Assigning: "r" = "s->len % 64UL". The value of "r" is now between 0 and 63 (inclusive).
util-linux-2.40-rc1/lib/sha256.c:92: incr: Incrementing "r". The value of "r" is now between 1 and 64 (inclusive).
util-linux-2.40-rc1/lib/sha256.c:93: cond_between: Checking "r > 56U" implies that "r" is between 57 and 64 (inclusive) on the true branch.
util-linux-2.40-rc1/lib/sha256.c:94: overrun-local: Overrunning array of 64 bytes at byte offset 64 by dereferencing pointer "s->buf + r". [Note: The source code implementation of the function has been overridden by a builtin model.]
#   92|   	s->buf[r++] = 0x80;
#   93|   	if (r > 56) {
#   94|-> 		memset(s->buf + r, 0, 64 - r);
#   95|   		r = 0;
#   96|   		processblock(s, s->buf);', true, 'Non-Issue', 'don''t want to touch SHA ...', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.249001');
INSERT INTO public.ground_truth VALUES (608, 'util-linux-2.40-0.8.rc1.el10', 29, 'Error: BAD_FREE (CWE-763):
util-linux-2.40-rc1/libmount/src/utils.c:1238: array_address: Taking address of array ""root="".
util-linux-2.40-rc1/libmount/src/utils.c:1238: identity_transfer: Passing ""root="" as argument 1 to function "mnt_get_kernel_cmdline_option", which returns that argument.
util-linux-2.40-rc1/libmount/src/utils.c:1238: assign: Assigning: "spec" = "mnt_get_kernel_cmdline_option("root=")".
util-linux-2.40-rc1/libmount/src/utils.c:1280: incorrect_free: "free" frees incorrect pointer "spec".
# 1278|   	}
# 1279|   done:
# 1280|-> 	free(spec);
# 1281|   	if (dev) {
# 1282|   		*path = allocated ? dev : strdup(dev);', true, 'Non-Issue', 'it returns allocated string or NULL if argument is foo=', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.249001');
INSERT INTO public.ground_truth VALUES (609, 'util-linux-2.40-0.8.rc1.el10', 30, 'Error: INTEGER_OVERFLOW (CWE-190):
util-linux-2.40-rc1/text-utils/ul.c:482: tainted_data_return: The value returned by "getwc(f)" is considered tainted.
util-linux-2.40-rc1/text-utils/ul.c:482: assign: Assigning: "c" = "getwc(f)".
util-linux-2.40-rc1/text-utils/ul.c:533: underflow: The cast of "c" to a signed type could result in a negative number.
#  531|   				/* non printable */
#  532|   				continue;
#  533|-> 			width = wcwidth(c);
#  534|   			need_column(ctl, ctl->column + width);
#  535|   			if (ctl->buf[ctl->column].c_char == ''\0'') {', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-38423

already fixt in upstream tree', 'Potential underflow at line 533 in `wcwidth(c)` call, as `c` (a `wint_t` from tainted `getwc(f)` at line 482) is cast to a signed type, with no explicit mitigation in the provided code (lines 477-563) to prevent underflow for all possible `c` values.', '2025-11-18 16:18:56.249001');
INSERT INTO public.ground_truth VALUES (610, 'util-linux-2.40-0.8.rc1.el10', 31, 'Error: UNINIT (CWE-457):
util-linux-2.40-rc1/libsmartcols/src/filter-parser.c:1194: var_decl: Declaring variable "yyvsa" without initializer.
util-linux-2.40-rc1/libsmartcols/src/filter-parser.c:1195: assign: Assigning: "yyvs" = "yyvsa", which points to uninitialized data.
util-linux-2.40-rc1/libsmartcols/src/filter-parser.c:1288: uninit_use_in_call: Using uninitialized value "*yyvs" when calling "__builtin_memcpy".
# 1286|             YYNOMEM;
# 1287|           YYSTACK_RELOCATE (yyss_alloc, yyss);
# 1288|->         YYSTACK_RELOCATE (yyvs_alloc, yyvs);
# 1289|   #  undef YYSTACK_RELOCATE
# 1290|           if (yyss1 != yyssa)', true, 'Non-Issue', 'code generated by bison', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.249001');
INSERT INTO public.ground_truth VALUES (611, 'util-linux-2.40-0.8.rc1.el10', 32, 'Error: UNINIT (CWE-457):
util-linux-2.40-rc1/libsmartcols/src/filter-parser.c:1194: var_decl: Declaring variable "yyvsa" without initializer.
util-linux-2.40-rc1/libsmartcols/src/filter-parser.c:1195: assign: Assigning: "yyvs" = "yyvsa", which points to uninitialized data.
util-linux-2.40-rc1/libsmartcols/src/filter-parser.c:1196: assign: Assigning: "yyvsp" = "yyvs", which points to uninitialized data.
util-linux-2.40-rc1/libsmartcols/src/filter-parser.c:1766: uninit_use_in_call: Using uninitialized value "*yyvsp" when calling "yydestruct".
# 1764|     while (yyssp != yyss)
# 1765|       {
# 1766|->       yydestruct ("Cleanup: popping",
# 1767|                     YY_ACCESSING_SYMBOL (+*yyssp), yyvsp, scanner, fltr);
# 1768|         YYPOPSTACK (1);', true, 'Non-Issue', 'code generated by bison', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.249001');
INSERT INTO public.ground_truth VALUES (612, 'util-linux-2.40-0.8.rc1.el10', 33, 'Error: USE_AFTER_FREE (CWE-416):
util-linux-2.40-rc1/sys-utils/lsmem.c:380: freed_arg: "free" frees "line".
util-linux-2.40-rc1/sys-utils/lsmem.c:391: pass_freed_arg: Passing freed pointer "line" as an argument to "strtok".
#  389|   	    && line) {
#  390|   
#  391|-> 		char *token = strtok(line, " ");
#  392|   
#  393|   		for (i = 0; token && i < MAX_NR_ZONES; i++) {', true, 'Non-Issue', 'it''s two independent use of the varaible', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.249001');
INSERT INTO public.ground_truth VALUES (613, 'util-linux-2.40-0.8.rc1.el10', 34, 'Error: RESOURCE_LEAK (CWE-772):
util-linux-2.40-rc1/libmount/python/fs.c:812: alloc_fn: Storage is returned from allocation function "mnt_copy_fs".
util-linux-2.40-rc1/libmount/python/fs.c:812: leaked_storage: Failing to save or free storage allocated by "mnt_copy_fs(((FsObject *)dest)->fs, self->fs)" leaks it.
#  810|   	}
#  811|   	if (PyObject_TypeCheck(dest, &FsType)) {	/* existing object passed as argument */
#  812|-> 		if (!mnt_copy_fs(((FsObject *)dest)->fs, self->fs))
#  813|   			return NULL;
#  814|   		DBG(FS, pymnt_debug_h(dest, "copy data"));', true, 'Non-Issue', 'new fs copy is handled by "dest"', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.249001');
INSERT INTO public.ground_truth VALUES (614, 'util-linux-2.40-0.8.rc1.el10', 35, 'Error: RESOURCE_LEAK (CWE-772):
util-linux-2.40-rc1/libblkid/src/topology/md.c:100: open_fn: Returning handle opened by "open". [Note: The source code implementation of the function has been overridden by a user model.]
util-linux-2.40-rc1/libblkid/src/topology/md.c:100: var_assign: Assigning: "fd" = handle returned from "open(diskpath, 524288)".
util-linux-2.40-rc1/libblkid/src/topology/md.c:109: noescape: Resource "fd" is not freed or pointed-to in "ioctl".
util-linux-2.40-rc1/libblkid/src/topology/md.c:145: leaked_handle: Handle variable "fd" going out of scope leaks the handle.
#  143|   	if (fd >= 0 && fd != pr->fd)
#  144|   		close(fd);
#  145|-> 	return 1;
#  146|   }
#  147|', true, 'Non-Issue', 'it;s closed if fd >= 0', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.249001');
INSERT INTO public.ground_truth VALUES (615, 'util-linux-2.40-0.8.rc1.el10', 36, 'Error: OVERRUN (CWE-119):
util-linux-2.40-rc1/libsmartcols/src/filter-parser.c:1186: assignment: Assigning: "yystacksize" = "200L".
util-linux-2.40-rc1/libsmartcols/src/filter-parser.c:1276: assignment: Assigning: "yystacksize" *= "2L". The value of "yystacksize" is now 400.
util-linux-2.40-rc1/libsmartcols/src/filter-parser.c:1287: alias: Assigning: "yyss" = "&yyptr->yyss_alloc". "yyss" now points to byte 0 of "yyptr->yyss_alloc" (which consists of 16 bytes).
util-linux-2.40-rc1/libsmartcols/src/filter-parser.c:1303: illegal_address: "yyss + yystacksize - 1" evaluates to an address that is at byte offset 399 of an array of 16 bytes.
# 1301|         YY_IGNORE_USELESS_CAST_END
# 1302|   
# 1303|->       if (yyss + yystacksize - 1 <= yyssp)
# 1304|           YYABORT;
# 1305|       }', true, 'Non-Issue', 'code generated by bison', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.249001');
INSERT INTO public.ground_truth VALUES (616, 'util-linux-2.40-0.8.rc1.el10', 37, 'Error: INTEGER_OVERFLOW (CWE-190):
util-linux-2.40-rc1/term-utils/setterm.c:918: tainted_data_return: Called function "read(0, retstr + pos, 31UL - pos)", and a possible return value may be less than zero.
util-linux-2.40-rc1/term-utils/setterm.c:918: assign: Assigning: "rc" = "read(0, retstr + pos, 31UL - pos)".
util-linux-2.40-rc1/term-utils/setterm.c:927: overflow: The expression "pos" is considered to have possibly overflowed.
util-linux-2.40-rc1/term-utils/setterm.c:918: overflow: The expression "31UL - pos" is deemed overflowed because at least one of its arguments has overflowed.
util-linux-2.40-rc1/term-utils/setterm.c:918: overflow_sink: "31UL - pos", which might have underflowed, is passed to "read(0, retstr + pos, 31UL - pos)". [Note: The source code implementation of the function has been overridden by a builtin model.]
#  916|   		if (0 == select_wait())
#  917|   			break;
#  918|-> 		if ((rc =
#  919|   		     read(STDIN_FILENO, retstr + pos,
#  920|   			  sizeof(retstr) - 1 - pos)) < 0) {', true, 'Non-Issue', 'I see protection agains overflow', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.249001');
INSERT INTO public.ground_truth VALUES (617, 'util-linux-2.40-0.8.rc1.el10', 38, 'Error: RESOURCE_LEAK (CWE-772):
util-linux-2.40-rc1/libmount/src/tab.c:1871: alloc_arg: "asprintf" allocates memory that is stored into "tgt_buf". [Note: The source code implementation of the function has been overridden by a builtin model.]
util-linux-2.40-rc1/libmount/src/tab.c:1875: var_assign: Assigning: "tgt" = "tgt_buf".
util-linux-2.40-rc1/libmount/src/tab.c:1879: noescape: Resource "tgt" is not freed or pointed-to in "mnt_fs_streq_target".
util-linux-2.40-rc1/libmount/src/tab.c:1867: var_assign: Assigning: "p" = "tgt".
util-linux-2.40-rc1/libmount/src/tab.c:1871: noescape: Assuming resource "p" is not freed or pointed-to as ellipsis argument to "asprintf".
util-linux-2.40-rc1/libmount/src/tab.c:1877: leaked_storage: Variable "p" going out of scope leaks the storage it points to.
# 1875|   					tgt = tgt_buf;
# 1876|   				}
# 1877|-> 			}
# 1878|   
# 1879|   			if (mnt_fs_streq_target(fs, tgt))', true, 'Non-Issue', 'athe code frees tgt_buf', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.249001');
INSERT INTO public.ground_truth VALUES (618, 'util-linux-2.40-0.8.rc1.el10', 39, 'Error: INTEGER_OVERFLOW (CWE-125):
util-linux-2.40-rc1/libfdisk/src/bsd.c:842: tainted_data_argument: The value "*l->bsdbuffer" is considered tainted.
util-linux-2.40-rc1/libfdisk/src/bsd.c:847: tainted_data_transitive: Call to function "memmove" with tainted argument "l->bsdbuffer" transitively taints "*d". [Note: The source code implementation of the function has been overridden by a builtin model.]
util-linux-2.40-rc1/libfdisk/src/bsd.c:855: tainted_data_argument: "d->d_npartitions" is considered tainted.
util-linux-2.40-rc1/libfdisk/src/bsd.c:855: cast_overflow: An assign that casts to a different type, which might trigger an overflow.
util-linux-2.40-rc1/libfdisk/src/bsd.c:856: deref_overflow: "t", which might have overflowed, is used in a pointer index in "d->d_partitions[t]".
#  854|   
#  855|   	for (t = d->d_npartitions; t < BSD_MAXPARTITIONS; t++) {
#  856|-> 		d->d_partitions[t].p_size   = 0;
#  857|   		d->d_partitions[t].p_offset = 0;
#  858|   		d->d_partitions[t].p_fstype = BSD_FS_UNUSED;', true, 'Non-Issue', 't < BSD_MAXPARTITION protect access to the array', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.249001');
INSERT INTO public.ground_truth VALUES (619, 'util-linux-2.40-0.8.rc1.el10', 40, 'Error: INTEGER_OVERFLOW (CWE-190):
util-linux-2.40-rc1/term-utils/wall.c:142: tainted_data_return: Called function "sysconf(_SC_NGROUPS_MAX)", and a possible return value may be less than zero.
util-linux-2.40-rc1/term-utils/wall.c:142: overflow: The expression "sysconf(_SC_NGROUPS_MAX) + 1L" is considered to have possibly overflowed.
util-linux-2.40-rc1/term-utils/wall.c:142: cast_overflow: An assign that casts to a different type, which might trigger an overflow.
util-linux-2.40-rc1/term-utils/wall.c:143: overflow_sink: "buf->ngroups", which might be negative, is passed to "xcalloc(buf->ngroups, 4UL)".
#  141|   	buf->requested_group = get_group_gid(group);
#  142|   	buf->ngroups = sysconf(_SC_NGROUPS_MAX) + 1;  /* room for the primary gid */
#  143|-> 	buf->groups = xcalloc(buf->ngroups, sizeof(*buf->groups));
#  144|   
#  145|   	return buf;', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-38423

fixed in v2.40.1', 'The code is vulnerable to overflow and undefined behavior due to the unvalidated assignment of `sysconf(_SC_NGROUPS_MAX) + 1` to `buf->ngroups`, which is then passed to `xcalloc` without checks for negative or zero values, aligning with the CVE''s descriptions of tainted data return, potential overflow, and lack of safeguards.', '2025-11-18 16:18:56.249001');
INSERT INTO public.ground_truth VALUES (620, 'util-linux-2.40-0.8.rc1.el10', 41, 'Error: RESOURCE_LEAK (CWE-772):
util-linux-2.40-rc1/libmount/src/context_mount.c:528: alloc_fn: Storage is returned from allocation function "strdup".
util-linux-2.40-rc1/libmount/src/context_mount.c:528: var_assign: Assigning: "org_type" = storage returned from "strdup(mnt_fs_get_fstype(cxt->fs))".
util-linux-2.40-rc1/libmount/src/context_mount.c:551: overwrite_var: Overwriting "org_type" in "org_type = NULL" leaks the storage that "org_type" points to.
#  549|   	if (org_type && rc != 0)
#  550|   		__mnt_fs_set_fstype_ptr(cxt->fs, org_type);
#  551|-> 	org_type  = NULL;
#  552|   
#  553|   	if (rc == 0 && try_type && cxt->update) {', false, 'Non-Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-38423

fixed in v2.40.1', 'Memory allocated by `strdup` at line 528 is explicitly freed at line 562 (`free(org_type);`), regardless of execution path, rendering the reported leak at line 551 (`org_type = NULL;`) a non-issue.', '2025-11-18 16:18:56.249001');
INSERT INTO public.ground_truth VALUES (621, 'util-linux-2.40-0.8.rc1.el10', 42, 'Error: RESOURCE_LEAK (CWE-772):
util-linux-2.40-rc1/libblkid/src/topology/md.c:100: open_fn: Returning handle opened by "open". [Note: The source code implementation of the function has been overridden by a user model.]
util-linux-2.40-rc1/libblkid/src/topology/md.c:100: var_assign: Assigning: "fd" = handle returned from "open(diskpath, 524288)".
util-linux-2.40-rc1/libblkid/src/topology/md.c:109: noescape: Resource "fd" is not freed or pointed-to in "ioctl".
util-linux-2.40-rc1/libblkid/src/topology/md.c:140: leaked_handle: Handle variable "fd" going out of scope leaks the handle.
#  138|   	blkid_topology_set_optimal_io_size(pr, (unsigned long) md.chunk_size * md.raid_disks);
#  139|   
#  140|-> 	return 0;
#  141|   
#  142|   nothing:', true, 'Non-Issue', 'I do not see a leak', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.249001');
INSERT INTO public.ground_truth VALUES (622, 'util-linux-2.40-0.8.rc1.el10', 43, 'Error: USE_AFTER_FREE (CWE-416):
util-linux-2.40-rc1/sys-utils/dmesg.c:1359: freed_arg: "get_next_syslog_record" frees "ctl->mmap_buff".
util-linux-2.40-rc1/sys-utils/dmesg.c:1359: double_free: Calling "get_next_syslog_record" frees pointer "ctl->mmap_buff" which has already been freed.
# 1357|   	}
# 1358|   
# 1359|-> 	while (get_next_syslog_record(ctl, &rec) == 0)
# 1360|   		print_record(ctl, &rec);
# 1361|   }', true, 'Non-Issue', 'it does not unmapp all buffer', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.249001');
INSERT INTO public.ground_truth VALUES (623, 'util-linux-2.40-0.8.rc1.el10', 44, 'Error: UNINIT (CWE-457):
util-linux-2.40-rc1/libfdisk/src/dos.c:793: var_decl: Declaring variable "t" without initializer.
util-linux-2.40-rc1/libfdisk/src/dos.c:858: uninit_use: Using uninitialized value "t[i].v".
#  856|   		for (i = 0; i + dif < 8; i++) {
#  857|   			j = i + dif;
#  858|-> 			if (!t[i].v || !t[j].v)
#  859|   				continue;
#  860|   			n1 = t[i].o * t[j].h;', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-38423

fixed in v2.40.1', 'Variable ''t'' is declared without an initializer (line 793) and its elements are only partially populated (lines 832-853), leading to potential use of uninitialized values in the loop at line 858, specifically for ''t[i].v'' or ''t[j].v'' when ''i'' or ''j'' indexes an unset element.', '2025-11-18 16:18:56.249001');
INSERT INTO public.ground_truth VALUES (624, 'util-linux-2.40-0.8.rc1.el10', 45, 'Error: USE_AFTER_FREE (CWE-416):
util-linux-2.40-rc1/term-utils/agetty.c:2031: identity_transfer: Passing field "ie->mem" (indirectly, via argument 1) to function "print_issue_file", which assigns it to "ie->mem_old".
util-linux-2.40-rc1/term-utils/agetty.c:2038: freed_arg: "eval_issue_file" frees "ie->mem".
util-linux-2.40-rc1/term-utils/agetty.c:2031: double_free: Calling "print_issue_file" frees pointer "ie->mem_old" which has already been freed.
# 2029|   again:
# 2030|   #endif
# 2031|-> 	print_issue_file(ie, op, tp);
# 2032|   
# 2033|   	if (op->flags & F_LOGINPAUSE) {', true, 'Non-Issue', 'the code sould be more robust', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.249001');
INSERT INTO public.ground_truth VALUES (625, 'util-linux-2.40-0.8.rc1.el10', 46, 'Error: NEGATIVE_RETURNS (CWE-394):
util-linux-2.40-rc1/misc-utils/lslocks.c:265: negative_return_fn: Function "readlinkat(fd, dp->d_name, sym, 4095UL)" returns a negative number. [Note: The source code implementation of the function has been overridden by a user model.]
util-linux-2.40-rc1/misc-utils/lslocks.c:265: assign: Assigning: "len" = "readlinkat(fd, dp->d_name, sym, 4095UL)".
util-linux-2.40-rc1/misc-utils/lslocks.c:269: negative_returns: Using variable "len" as an index to array "sym".
#  267|   
#  268|   		*size = sb.st_size;
#  269|-> 		sym[len] = ''\0'';
#  270|   
#  271|   		ret = xstrdup(sym);', true, 'Non-Issue', 'there is if < 0', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.249001');
INSERT INTO public.ground_truth VALUES (626, 'util-linux-2.40-0.8.rc1.el10', 47, 'Error: RESOURCE_LEAK (CWE-772):
util-linux-2.40-rc1/sys-utils/swapon.c:786: alloc_fn: Storage is returned from allocation function "mnt_resolve_spec".
util-linux-2.40-rc1/sys-utils/swapon.c:786: var_assign: Assigning: "device" = storage returned from "mnt_resolve_spec(mnt_fs_get_source(fs), mntcache)".
util-linux-2.40-rc1/sys-utils/swapon.c:793: noescape: Resource "device" is not freed or pointed-to in "is_active_swap".
util-linux-2.40-rc1/sys-utils/swapon.c:796: leaked_storage: Variable "device" going out of scope leaks the storage it points to.
#  794|   			if (ctl->verbose)
#  795|   				warnx(_("%s: already active -- ignored"), device);
#  796|-> 			continue;
#  797|   		}
#  798|', true, 'Non-Issue', 'the pointer is maintained by mntcache', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.249001');
INSERT INTO public.ground_truth VALUES (627, 'util-linux-2.40-0.8.rc1.el10', 48, 'Error: RESOURCE_LEAK (CWE-772):
util-linux-2.40-rc1/sys-utils/hwclock-rtc.c:483: alloc_fn: Storage is returned from allocation function "xstrdup".
util-linux-2.40-rc1/sys-utils/hwclock-rtc.c:483: var_assign: Assigning: "opt" = storage returned from "xstrdup(opt0)".
util-linux-2.40-rc1/sys-utils/hwclock-rtc.c:486: noescape: Resource "opt" is not freed or pointed-to in "strtok".
util-linux-2.40-rc1/sys-utils/hwclock-rtc.c:508: leaked_storage: Variable "opt" going out of scope leaks the storage it points to.
#  506|   	if (rtc_fd < 0) {
#  507|   		warnx(_("cannot open %s"), rtc_dev_name);
#  508|-> 		return 1;
#  509|   	}
#  510|', false, 'Non-Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-38423

fixed in v2.40.1', 'Storage allocated by ''xstrdup'' for ''opt'' is freed at line 523, reachable via all exit paths (including conditional returns and errors), rendering the reported storage leak (CWE-772) a FALSE POSITIVE.', '2025-11-18 16:18:56.249001');
INSERT INTO public.ground_truth VALUES (628, 'util-linux-2.40-0.8.rc1.el10', 49, 'Error: UNINIT (CWE-457):
util-linux-2.40-rc1/libfdisk/src/dos.c:793: var_decl: Declaring variable "t" without initializer.
util-linux-2.40-rc1/libfdisk/src/dos.c:860: uninit_use: Using uninitialized value "t[j].h".
#  858|   			if (!t[i].v || !t[j].v)
#  859|   				continue;
#  860|-> 			n1 = t[i].o * t[j].h;
#  861|   			n2 = t[j].o * t[i].h;
#  862|   			n3 = t[j].o * t[i].c;', false, 'Non-Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-38423

fixed in v2.40.1', 'Variable ''t'' is initialized through the ''chs_set_t'' macro (lines 801, 842, 852) before its elements, including ''t[j].h'', are used at line 860, with additional safeguard at lines 858-859 preventing use of uninitialized values.', '2025-11-18 16:18:56.249001');
INSERT INTO public.ground_truth VALUES (629, 'util-linux-2.40-0.8.rc1.el10', 50, 'Error: RESOURCE_LEAK (CWE-772):
util-linux-2.40-rc1/login-utils/login.c:438: open_fn: Returning handle opened by "open". [Note: The source code implementation of the function has been overridden by a user model.]
util-linux-2.40-rc1/login-utils/login.c:438: var_assign: Assigning: "fd" = handle returned from "open(tty, 2050)".
util-linux-2.40-rc1/login-utils/login.c:444: noescape: Resource "fd" is not freed or pointed-to in "isatty".
util-linux-2.40-rc1/login-utils/login.c:450: noescape: Resource "fd" is not freed or pointed-to in "fcntl".
util-linux-2.40-rc1/login-utils/login.c:452: noescape: Resource "fd" is not freed or pointed-to in "fcntl".
util-linux-2.40-rc1/login-utils/login.c:458: noescape: Resource "fd" is not freed or pointed-to in "dup2".
util-linux-2.40-rc1/login-utils/login.c:461: leaked_handle: Handle variable "fd" going out of scope leaks the handle.
#  459|   	if (fd >= 3)
#  460|   		close(fd);
#  461|-> }
#  462|   
#  463|   static inline void chown_err(const char *what, uid_t uid, gid_t gid)', true, 'Non-Issue', 'wanted and expected, it''s tty and must be keep open', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.249001');
INSERT INTO public.ground_truth VALUES (1, 'adcli-0.9.2-6.el10', 1, 'Error: RESOURCE_LEAK (CWE-772):
adcli-0.9.2/library/adconn.c:409: alloc_arg: "asprintf" allocates memory that is stored into "filename". [Note: The source code implementation of the function has been overridden by a builtin model.]
adcli-0.9.2/library/adconn.c:436: noescape: Resource "filename" is not freed or pointed-to in "mkstemp".
adcli-0.9.2/library/adconn.c:468: leaked_storage: Variable "filename" going out of scope leaks the storage it points to.
#  466|   
#  467|   	/* This shouldn''t stop joining */
#  468|-> 	return ADCLI_SUCCESS;
#  469|   }
#  470|', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-45146

', 'Memory allocated for `filename` by `asprintf` (line 409) is not explicitly freed in all execution paths, including error handling (e.g., `if (fd < 0)`) and upon successful execution, before going out of scope (line 468), indicating a potential resource leak (CWE-772).', '2025-11-18 16:18:56.017309');
INSERT INTO public.ground_truth VALUES (2, 'adcli-0.9.2-6.el10', 2, 'Error: RESOURCE_LEAK (CWE-772):
adcli-0.9.2/tools/computer.c:443: alloc_fn: Storage is returned from allocation function "adcli_enroll_new".
adcli-0.9.2/tools/computer.c:443: var_assign: Assigning: "enroll" = storage returned from "adcli_enroll_new(conn)".
adcli-0.9.2/tools/computer.c:491: noescape: Resource "enroll" is not freed or pointed-to in "adcli_enroll_unref".
adcli-0.9.2/tools/computer.c:492: leaked_storage: Variable "enroll" going out of scope leaks the storage it points to.
#  490|   		warnx ("extra arguments specified");
#  491|   		adcli_enroll_unref (enroll);
#  492|-> 		return 2;
#  493|   	}
#  494|', true, 'Non-Issue', '`enroll` is not freed directly in `adcli_unref_enroll()` but in a funtion called in `adcli_unret_enroll()`', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.017309');
INSERT INTO public.ground_truth VALUES (3, 'adcli-0.9.2-6.el10', 3, 'Error: BAD_FREE (CWE-763):
adcli-0.9.2/library/adenroll.c:2343: array_address: Taking address of array """".
adcli-0.9.2/library/adenroll.c:2343: assign: Assigning: "name" = """".
adcli-0.9.2/library/adenroll.c:2346: incorrect_free: "krb5_free_unparsed_name" frees incorrect pointer "name".
# 2344|   		res = add_principal_to_keytab (enroll, k5, enroll->keytab_principals[i],
# 2345|   		                               name, &which_salt, flags);
# 2346|-> 		krb5_free_unparsed_name (k5, name);
# 2347|   
# 2348|   		if (res != ADCLI_SUCCESS)', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-45146

', 'Assignment of a string literal to pointer `name` at line 2343, followed by an incorrect attempt to free it using `krb5_free_unparsed_name` at line 2346, constitutes undefined behavior and directly correlates with the CVE''s description of freeing an incorrect pointer.', '2025-11-18 16:18:56.017309');
INSERT INTO public.ground_truth VALUES (4, 'adcli-0.9.2-6.el10', 4, 'Error: RESOURCE_LEAK (CWE-772):
adcli-0.9.2/library/adenroll.c:1524: alloc_fn: Storage is returned from allocation function "krb5_get_error_message".
adcli-0.9.2/library/adenroll.c:1524: noescape: Resource "krb5_get_error_message(k5, code)" is not freed or pointed-to in "_adcli_err".
adcli-0.9.2/library/adenroll.c:1524: leaked_storage: Failing to save or free storage allocated by "krb5_get_error_message(k5, code)" leaks it.
# 1522|   
# 1523|   	if (code != 0) {
# 1524|-> 		_adcli_err ("Couldn''t set password for %s account: %s: %s",
# 1525|   		            s_or_c (enroll),
# 1526|   		            enroll->computer_sam, krb5_get_error_message (k5, code));', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-45146

', 'Storage allocated by `krb5_get_error_message(k5, code)` at line 1524 is not explicitly freed within the provided code scope, directly correlating with CWE-772 (Resource Leak) as reported, with no visible deallocation in the `_adcli_err` function or surrounding code.', '2025-11-18 16:18:56.017309');
INSERT INTO public.ground_truth VALUES (5, 'adcli-0.9.2-6.el10', 5, 'Error: USE_AFTER_FREE (CWE-416):
adcli-0.9.2/library/adutil.c:887: freed_arg: "free" frees "stdout_data".
adcli-0.9.2/library/adutil.c:896: deref_arg: Calling "strncmp" dereferences freed pointer "(char *)stdout_data".
#  894|   	                                    &stdout_data, &stdout_data_len);
#  895|   	assert (res == ADCLI_SUCCESS);
#  896|-> 	assert (strncmp ("Hello\n", (char *) stdout_data, stdout_data_len) == 0);
#  897|   	free (stdout_data);
#  898|   #endif', true, 'Non-Issue', 'After the `free()` in line 887 there is a function call which allocates new memory for `stdout_data`.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.017309');
INSERT INTO public.ground_truth VALUES (6, 'adcli-0.9.2-6.el10', 6, 'Error: RESOURCE_LEAK (CWE-772):
adcli-0.9.2/tools/computer.c:884: alloc_fn: Storage is returned from allocation function "adcli_enroll_new".
adcli-0.9.2/tools/computer.c:884: var_assign: Assigning: "enroll" = storage returned from "adcli_enroll_new(conn)".
adcli-0.9.2/tools/computer.c:900: noescape: Resource "enroll" is not freed or pointed-to in "parse_option".
adcli-0.9.2/tools/computer.c:902: noescape: Resource "enroll" is not freed or pointed-to in "adcli_enroll_unref".
adcli-0.9.2/tools/computer.c:903: leaked_storage: Variable "enroll" going out of scope leaks the storage it points to.
#  901|   			if (res != ADCLI_SUCCESS) {
#  902|   				adcli_enroll_unref (enroll);
#  903|-> 				return res;
#  904|   			}
#  905|   			break;', true, 'Non-Issue', '`enroll` is not freed directly in `adcli_unref_enroll()` but in a funtion called in `adcli_unret_enroll()`', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.017309');
INSERT INTO public.ground_truth VALUES (7, 'adcli-0.9.2-6.el10', 7, 'Error: RESOURCE_LEAK (CWE-772):
adcli-0.9.2/library/adconn.c:366: alloc_fn: Storage is returned from allocation function "krb5_get_error_message".
adcli-0.9.2/library/adconn.c:366: noescape: Resource "krb5_get_error_message(conn->k5, code)" is not freed or pointed-to in "_adcli_err".
adcli-0.9.2/library/adconn.c:366: leaked_storage: Failing to save or free storage allocated by "krb5_get_error_message(conn->k5, code)" leaks it.
#  364|   	} else {
#  365|   		if (type == ADCLI_LOGIN_COMPUTER_ACCOUNT) {
#  366|-> 			_adcli_err ("Couldn''t get kerberos ticket for machine account: %s: %s",
#  367|   			            name, krb5_get_error_message (conn->k5, code));
#  368|   		} else {', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-45146

', 'Storage allocated by `krb5_get_error_message` at `adconn.c:366` is not explicitly freed within the provided code, and the unclear implementation of `_adcli_err` (declared in `adprivate.h`) prevents assumption of automatic memory deallocation, directly correlating with a Resource Leak (CWE-772).', '2025-11-18 16:18:56.017309');
INSERT INTO public.ground_truth VALUES (8, 'adcli-0.9.2-6.el10', 8, 'Error: RESOURCE_LEAK (CWE-772):
adcli-0.9.2/library/adenroll.c:1597: alloc_fn: Storage is returned from allocation function "krb5_get_error_message".
adcli-0.9.2/library/adenroll.c:1597: noescape: Resource "krb5_get_error_message(k5, code)" is not freed or pointed-to in "_adcli_err".
adcli-0.9.2/library/adenroll.c:1597: leaked_storage: Failing to save or free storage allocated by "krb5_get_error_message(k5, code)" leaks it.
# 1595|   
# 1596|   	if (code != 0) {
# 1597|-> 		_adcli_err ("Couldn''t change password for %s account: %s: %s",
# 1598|   		            s_or_c (enroll),
# 1599|   		            enroll->computer_sam, krb5_get_error_message (k5, code));', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-45146

', 'Storage allocated by `krb5_get_error_message(k5, code)` at line 1597 is not explicitly freed or handled within the provided code scope, including within the `_adcli_err` function call, supporting the leaked storage claim.', '2025-11-18 16:18:56.017309');
INSERT INTO public.ground_truth VALUES (9, 'adcli-0.9.2-6.el10', 9, 'Error: RESOURCE_LEAK (CWE-772):
adcli-0.9.2/library/adkrb5.c:202: alloc_fn: Storage is returned from allocation function "krb5_get_error_message".
adcli-0.9.2/library/adkrb5.c:202: noescape: Resource "krb5_get_error_message(k5, code)" is not freed or pointed-to in "_adcli_err".
adcli-0.9.2/library/adkrb5.c:202: leaked_storage: Failing to save or free storage allocated by "krb5_get_error_message(k5, code)" leaks it.
#  200|   		code = krb5_kt_default (k5, keytab);
#  201|   		if (code != 0) {
#  202|-> 			_adcli_err ("Failed to open default keytab: %s",
#  203|   			            krb5_get_error_message (k5, code));
#  204|   			return ADCLI_ERR_FAIL;', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-45146

', 'Storage allocated by `krb5_get_error_message(k5, code)` at line 202 is passed to `_adcli_err` without being freed, and no freeing function is called within the provided error handling block (lines 201-204), with no explicit evidence of internal memory management within `_adcli_err`.', '2025-11-18 16:18:56.017309');
INSERT INTO public.ground_truth VALUES (10, 'adcli-0.9.2-6.el10', 10, 'Error: RESOURCE_LEAK (CWE-772):
adcli-0.9.2/tools/computer.c:777: alloc_fn: Storage is returned from allocation function "adcli_enroll_new".
adcli-0.9.2/tools/computer.c:777: var_assign: Assigning: "enroll" = storage returned from "adcli_enroll_new(conn)".
adcli-0.9.2/tools/computer.c:798: noescape: Resource "enroll" is not freed or pointed-to in "adcli_enroll_unref".
adcli-0.9.2/tools/computer.c:799: leaked_storage: Variable "enroll" going out of scope leaks the storage it points to.
#  797|   			adcli_tool_usage (options, common_usages);
#  798|   			adcli_enroll_unref (enroll);
#  799|-> 			return 2;
#  800|   		default:
#  801|   			res = parse_option ((Option)opt, optarg, conn, enroll);', true, 'Non-Issue', '`enroll` is not freed directly in `adcli_unref_enroll()` but in a funtion called in `adcli_unret_enroll()`', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.017309');
INSERT INTO public.ground_truth VALUES (11, 'adcli-0.9.2-6.el10', 11, 'Error: RESOURCE_LEAK (CWE-772):
adcli-0.9.2/tools/computer.c:1069: alloc_fn: Storage is returned from allocation function "adcli_enroll_new".
adcli-0.9.2/tools/computer.c:1069: var_assign: Assigning: "enroll" = storage returned from "adcli_enroll_new(conn)".
adcli-0.9.2/tools/computer.c:1082: noescape: Resource "enroll" is not freed or pointed-to in "adcli_enroll_unref".
adcli-0.9.2/tools/computer.c:1083: leaked_storage: Variable "enroll" going out of scope leaks the storage it points to.
# 1081|   			adcli_tool_usage (options, common_usages);
# 1082|   			adcli_enroll_unref (enroll);
# 1083|-> 			return opt == ''h'' ? 0 : 2;
# 1084|   		default:
# 1085|   			res = parse_option ((Option)opt, optarg, conn, enroll);', true, 'Non-Issue', '`enroll` is not freed directly in `adcli_unref_enroll()` but in a funtion called in `adcli_unret_enroll()`', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.017309');
INSERT INTO public.ground_truth VALUES (12, 'adcli-0.9.2-6.el10', 12, 'Error: UNINIT (CWE-457):
adcli-0.9.2/library/test.c:182: var_decl: Declaring variable "item" without initializer.
adcli-0.9.2/library/test.c:188: uninit_use_in_call: Using uninitialized value "item". Field "item.next" is uninitialized when calling "test_push".
#  186|   	item.x.fix.teardown = teardown;
#  187|   
#  188|-> 	test_push (&item);
#  189|   }
#  190|', true, 'Non-Issue', 'It is expected that `item.next` is not set, it will be set explicitly in `test_push()`.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.017309');
INSERT INTO public.ground_truth VALUES (13, 'adcli-0.9.2-6.el10', 13, 'Error: RESOURCE_LEAK (CWE-772):
adcli-0.9.2/tools/computer.c:777: alloc_fn: Storage is returned from allocation function "adcli_enroll_new".
adcli-0.9.2/tools/computer.c:777: var_assign: Assigning: "enroll" = storage returned from "adcli_enroll_new(conn)".
adcli-0.9.2/tools/computer.c:820: noescape: Resource "enroll" is not freed or pointed-to in "adcli_enroll_get_computer_password".
adcli-0.9.2/tools/computer.c:827: noescape: Resource "enroll" is not freed or pointed-to in "adcli_enroll_unref".
adcli-0.9.2/tools/computer.c:828: leaked_storage: Variable "enroll" going out of scope leaks the storage it points to.
#  826|   		       adcli_get_last_error ());
#  827|   		adcli_enroll_unref (enroll);
#  828|-> 		return -res;
#  829|   	}
#  830|', true, 'Non-Issue', '`enroll` is not freed directly in `adcli_unref_enroll()` but in a funtion called in `adcli_unret_enroll()`', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.017309');
INSERT INTO public.ground_truth VALUES (14, 'adcli-0.9.2-6.el10', 14, 'Error: RESOURCE_LEAK (CWE-772):
adcli-0.9.2/library/adenroll.c:2315: alloc_fn: Storage is returned from allocation function "krb5_get_error_message".
adcli-0.9.2/library/adenroll.c:2315: noescape: Resource "krb5_get_error_message(k5, code)" is not freed or pointed-to in "_adcli_err".
adcli-0.9.2/library/adenroll.c:2315: leaked_storage: Failing to save or free storage allocated by "krb5_get_error_message(k5, code)" leaks it.
# 2313|   
# 2314|   	if (code != 0) {
# 2315|-> 		_adcli_err ("Couldn''t add keytab entries: %s: %s",
# 2316|   		            enroll->keytab_name, krb5_get_error_message (k5, code));
# 2317|   		return ADCLI_ERR_FAIL;', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-45146

', 'Storage allocated by `krb5_get_error_message` at line 2315 is passed to `_adcli_err` without visible deallocation, leading to a potential leak, as no corresponding free function is called within the provided code scope.', '2025-11-18 16:18:56.017309');
INSERT INTO public.ground_truth VALUES (15, 'adcli-0.9.2-6.el10', 15, 'Error: RESOURCE_LEAK (CWE-772):
adcli-0.9.2/library/adenroll.c:2298: alloc_fn: Storage is returned from allocation function "krb5_get_error_message".
adcli-0.9.2/library/adenroll.c:2298: noescape: Resource "krb5_get_error_message(k5, code)" is not freed or pointed-to in "_adcli_warn".
adcli-0.9.2/library/adenroll.c:2298: leaked_storage: Failing to save or free storage allocated by "krb5_get_error_message(k5, code)" leaks it.
# 2296|   			                                         enctypes, salts, which_salt);
# 2297|   			if (code != 0) {
# 2298|-> 				_adcli_warn ("Couldn''t authenticate with keytab while discovering which salt to use: %s: %s",
# 2299|   				             principal_name, krb5_get_error_message (k5, code));
# 2300|   				*which_salt = DEFAULT_SALT;', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-45146

', 'Storage allocated by `krb5_get_error_message(k5, code)` at line 2298 is not freed or stored for later freeing within the `_adcli_warn` call, leading to a resource leak when `code != 0`.', '2025-11-18 16:18:56.017309');
INSERT INTO public.ground_truth VALUES (16, 'adcli-0.9.2-6.el10', 16, 'Error: RESOURCE_LEAK (CWE-772):
adcli-0.9.2/tools/computer.c:443: alloc_fn: Storage is returned from allocation function "adcli_enroll_new".
adcli-0.9.2/tools/computer.c:443: var_assign: Assigning: "enroll" = storage returned from "adcli_enroll_new(conn)".
adcli-0.9.2/tools/computer.c:472: noescape: Resource "enroll" is not freed or pointed-to in "adcli_enroll_unref".
adcli-0.9.2/tools/computer.c:473: leaked_storage: Variable "enroll" going out of scope leaks the storage it points to.
#  471|   			adcli_tool_usage (options, common_usages);
#  472|   			adcli_enroll_unref (enroll);
#  473|-> 			return opt == ''h'' ? 0 : 2;
#  474|   		default:
#  475|   			res = parse_option ((Option)opt, optarg, conn, enroll);', true, 'Non-Issue', '`enroll` is not freed directly in `adcli_unref_enroll()` but in a funtion called in `adcli_unret_enroll()`', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.017309');
INSERT INTO public.ground_truth VALUES (17, 'adcli-0.9.2-6.el10', 17, 'Error: RESOURCE_LEAK (CWE-772):
adcli-0.9.2/tools/computer.c:972: alloc_fn: Storage is returned from allocation function "adcli_enroll_new".
adcli-0.9.2/tools/computer.c:972: var_assign: Assigning: "enroll" = storage returned from "adcli_enroll_new(conn)".
adcli-0.9.2/tools/computer.c:985: noescape: Resource "enroll" is not freed or pointed-to in "adcli_enroll_unref".
adcli-0.9.2/tools/computer.c:986: leaked_storage: Variable "enroll" going out of scope leaks the storage it points to.
#  984|   			adcli_tool_usage (options, common_usages);
#  985|   			adcli_enroll_unref (enroll);
#  986|-> 			return opt == ''h'' ? 0 : 2;
#  987|   		default:
#  988|   			res = parse_option ((Option)opt, optarg, conn, enroll);', true, 'Non-Issue', '`enroll` is not freed directly in `adcli_unref_enroll()` but in a funtion called in `adcli_unret_enroll()`', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.017309');
INSERT INTO public.ground_truth VALUES (18, 'adcli-0.9.2-6.el10', 18, 'Error: RESOURCE_LEAK (CWE-772):
adcli-0.9.2/tools/computer.c:1170: alloc_fn: Storage is returned from allocation function "adcli_enroll_new".
adcli-0.9.2/tools/computer.c:1170: var_assign: Assigning: "enroll" = storage returned from "adcli_enroll_new(conn)".
adcli-0.9.2/tools/computer.c:1193: noescape: Resource "enroll" is not freed or pointed-to in "adcli_enroll_unref".
adcli-0.9.2/tools/computer.c:1194: leaked_storage: Variable "enroll" going out of scope leaks the storage it points to.
# 1192|   			adcli_tool_usage (options, common_usages);
# 1193|   			adcli_enroll_unref (enroll);
# 1194|-> 			return opt == ''h'' ? 0 : 2;
# 1195|   		default:
# 1196|   			res = parse_option ((Option)opt, optarg, conn, enroll);', true, 'Non-Issue', '`enroll` is not freed directly in `adcli_unref_enroll()` but in a funtion called in `adcli_unret_enroll()`', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.017309');
INSERT INTO public.ground_truth VALUES (19, 'adcli-0.9.2-6.el10', 19, 'Error: RESOURCE_LEAK (CWE-772):
adcli-0.9.2/library/adenroll.c:550: alloc_fn: Storage is returned from allocation function "krb5_get_error_message".
adcli-0.9.2/library/adenroll.c:550: noescape: Resource "krb5_get_error_message(k5, code)" is not freed or pointed-to in "_adcli_err".
adcli-0.9.2/library/adenroll.c:550: leaked_storage: Failing to save or free storage allocated by "krb5_get_error_message(k5, code)" leaks it.
#  548|   		if (code != 0) {
#  549|   			if (code != 0) {
#  550|-> 				_adcli_err ("Couldn''t parse kerberos user principal: %s: %s",
#  551|   				            enroll->user_principal,
#  552|   				            krb5_get_error_message (k5, code));', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-45146

', 'Storage allocated by `krb5_get_error_message` at line 552 is not freed or stored for later freeing within `_adcli_err` (lines 548-552), with no implicit memory management suggested by its `printf`-like declaration (adprivate.h, line 107).', '2025-11-18 16:18:56.017309');
INSERT INTO public.ground_truth VALUES (20, 'adcli-0.9.2-6.el10', 20, 'Error: RESOURCE_LEAK (CWE-772):
adcli-0.9.2/library/adconn.c:356: alloc_fn: Storage is returned from allocation function "krb5_get_error_message".
adcli-0.9.2/library/adconn.c:356: noescape: Resource "krb5_get_error_message(conn->k5, code)" is not freed or pointed-to in "_adcli_err".
adcli-0.9.2/library/adconn.c:356: leaked_storage: Failing to save or free storage allocated by "krb5_get_error_message(conn->k5, code)" leaks it.
#  354|   	           code == KRB5_PREAUTH_FAILED) {
#  355|   		if (type == ADCLI_LOGIN_COMPUTER_ACCOUNT) {
#  356|-> 			_adcli_err ("Couldn''t authenticate as machine account: %s: %s",
#  357|   			            name, krb5_get_error_message (conn->k5, code));
#  358|   		} else {', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-45146

', 'Storage allocated by `krb5_get_error_message` at line 356 is not explicitly freed or stored for later freeing within the `_adcli_err` function, leading to a potential resource leak (CWE-772) when the conditional execution path is taken.', '2025-11-18 16:18:56.017309');
INSERT INTO public.ground_truth VALUES (21, 'adcli-0.9.2-6.el10', 21, 'Error: RESOURCE_LEAK (CWE-772):
adcli-0.9.2/tools/computer.c:1069: alloc_fn: Storage is returned from allocation function "adcli_enroll_new".
adcli-0.9.2/tools/computer.c:1069: var_assign: Assigning: "enroll" = storage returned from "adcli_enroll_new(conn)".
adcli-0.9.2/tools/computer.c:1085: noescape: Resource "enroll" is not freed or pointed-to in "parse_option".
adcli-0.9.2/tools/computer.c:1087: noescape: Resource "enroll" is not freed or pointed-to in "adcli_enroll_unref".
adcli-0.9.2/tools/computer.c:1088: leaked_storage: Variable "enroll" going out of scope leaks the storage it points to.
# 1086|   			if (res != ADCLI_SUCCESS) {
# 1087|   				adcli_enroll_unref (enroll);
# 1088|-> 				return res;
# 1089|   			}
# 1090|   			break;', true, 'Non-Issue', '`enroll` is not freed directly in `adcli_unref_enroll()` but in a funtion called in `adcli_unret_enroll()`', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.017309');
INSERT INTO public.ground_truth VALUES (22, 'adcli-0.9.2-6.el10', 22, 'Error: USE_AFTER_FREE (CWE-416):
adcli-0.9.2/library/adutil.c:878: freed_arg: "free" frees "stdout_data".
adcli-0.9.2/library/adutil.c:886: deref_arg: Calling "strncmp" dereferences freed pointer "(char *)stdout_data".
#  884|   	                                    &stdout_data, &stdout_data_len);
#  885|   	assert (res == ADCLI_SUCCESS);
#  886|-> 	assert (strncmp ("World\nHello\n", (char *) stdout_data, stdout_data_len) == 0);
#  887|   	free (stdout_data);
#  888|   #endif', true, 'Non-Issue', 'After the `free()` in line 878 there is a function call which allocates new memory for `stdout_data`.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.017309');
INSERT INTO public.ground_truth VALUES (23, 'adcli-0.9.2-6.el10', 23, 'Error: RESOURCE_LEAK (CWE-772):
adcli-0.9.2/tools/computer.c:683: alloc_fn: Storage is returned from allocation function "adcli_enroll_new".
adcli-0.9.2/tools/computer.c:683: var_assign: Assigning: "enroll" = storage returned from "adcli_enroll_new(conn)".
adcli-0.9.2/tools/computer.c:696: noescape: Resource "enroll" is not freed or pointed-to in "adcli_enroll_unref".
adcli-0.9.2/tools/computer.c:697: leaked_storage: Variable "enroll" going out of scope leaks the storage it points to.
#  695|   			adcli_tool_usage (options, common_usages);
#  696|   			adcli_enroll_unref (enroll);
#  697|-> 			return opt == ''h'' ? 0 : 2;
#  698|   		default:
#  699|   			res = parse_option ((Option)opt, optarg, conn, enroll);', true, 'Non-Issue', '`enroll` is not freed directly in `adcli_unref_enroll()` but in a funtion called in `adcli_unret_enroll()`', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.017309');
INSERT INTO public.ground_truth VALUES (24, 'adcli-0.9.2-6.el10', 24, 'Error: RESOURCE_LEAK (CWE-772):
adcli-0.9.2/tools/computer.c:884: alloc_fn: Storage is returned from allocation function "adcli_enroll_new".
adcli-0.9.2/tools/computer.c:884: var_assign: Assigning: "enroll" = storage returned from "adcli_enroll_new(conn)".
adcli-0.9.2/tools/computer.c:914: noescape: Resource "enroll" is not freed or pointed-to in "adcli_enroll_unref".
adcli-0.9.2/tools/computer.c:915: leaked_storage: Variable "enroll" going out of scope leaks the storage it points to.
#  913|   		warnx ("specify one host name of computer account to reset");
#  914|   		adcli_enroll_unref (enroll);
#  915|-> 		return EUSAGE;
#  916|   	}
#  917|', true, 'Non-Issue', '`enroll` is not freed directly in `adcli_unref_enroll()` but in a funtion called in `adcli_unret_enroll()`', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.017309');
INSERT INTO public.ground_truth VALUES (25, 'adcli-0.9.2-6.el10', 25, 'Error: INTEGER_OVERFLOW (CWE-190):
adcli-0.9.2/library/adutil.c:498: tainted_data_return: Called function "write(fd, buf, len)", and a possible return value may be less than zero.
adcli-0.9.2/library/adutil.c:498: cast_overflow: An assign that casts to a different type, which might trigger an overflow.
adcli-0.9.2/library/adutil.c:504: overflow: The expression "len" is considered to have possibly overflowed.
adcli-0.9.2/library/adutil.c:498: overflow_sink: "len", which might have overflowed, is passed to "write(fd, buf, len)".
#  496|   
#  497|   	while (len > 0) {
#  498|-> 		res = write (fd, buf, len);
#  499|   		if (res <= 0) {
#  500|   			if (errno == EAGAIN || errno == EINTR)', true, 'Non-Issue', 'Negative return values of `write()` are properly checked and `len` is only modified for positiv return values.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.017309');
INSERT INTO public.ground_truth VALUES (26, 'adcli-0.9.2-6.el10', 26, 'Error: RESOURCE_LEAK (CWE-772):
adcli-0.9.2/tools/computer.c:575: alloc_fn: Storage is returned from allocation function "adcli_enroll_new".
adcli-0.9.2/tools/computer.c:575: var_assign: Assigning: "enroll" = storage returned from "adcli_enroll_new(conn)".
adcli-0.9.2/tools/computer.c:600: noescape: Resource "enroll" is not freed or pointed-to in "adcli_enroll_unref".
adcli-0.9.2/tools/computer.c:601: leaked_storage: Variable "enroll" going out of scope leaks the storage it points to.
#  599|   			adcli_tool_usage (options, common_usages);
#  600|   			adcli_enroll_unref (enroll);
#  601|-> 			return opt == ''h'' ? 0 : 2;
#  602|   		default:
#  603|   			res = parse_option ((Option)opt, optarg, conn, enroll);', true, 'Non-Issue', '`enroll` is not freed directly in `adcli_unref_enroll()` but in a funtion called in `adcli_unret_enroll()`', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.017309');
INSERT INTO public.ground_truth VALUES (27, 'adcli-0.9.2-6.el10', 27, 'Error: RESOURCE_LEAK (CWE-772):
adcli-0.9.2/library/adenroll.c:2227: alloc_fn: Storage is returned from allocation function "krb5_get_error_message".
adcli-0.9.2/library/adenroll.c:2227: noescape: Resource "krb5_get_error_message(k5, code)" is not freed or pointed-to in "_adcli_err".
adcli-0.9.2/library/adenroll.c:2227: leaked_storage: Failing to save or free storage allocated by "krb5_get_error_message(k5, code)" leaks it.
# 2225|   
# 2226|   	if (code != 0) {
# 2227|-> 		_adcli_err ("Couldn''t update keytab: %s: %s",
# 2228|   		            enroll->keytab_name, krb5_get_error_message (k5, code));
# 2229|   		return ADCLI_ERR_FAIL;', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-45146

', 'Storage allocated by `krb5_get_error_message` at line 2227 is passed to `_adcli_err` without being stored for potential freeing, and no corresponding deallocation is visible within the provided code snippet (lines 2226-2229), directly correlating with the described RESOURCE_LEAK (CWE-772) vulnerability.', '2025-11-18 16:18:56.017309');
INSERT INTO public.ground_truth VALUES (28, 'adcli-0.9.2-6.el10', 28, 'Error: RESOURCE_LEAK (CWE-772):
adcli-0.9.2/tools/computer.c:683: alloc_fn: Storage is returned from allocation function "adcli_enroll_new".
adcli-0.9.2/tools/computer.c:683: var_assign: Assigning: "enroll" = storage returned from "adcli_enroll_new(conn)".
adcli-0.9.2/tools/computer.c:699: noescape: Resource "enroll" is not freed or pointed-to in "parse_option".
adcli-0.9.2/tools/computer.c:701: noescape: Resource "enroll" is not freed or pointed-to in "adcli_enroll_unref".
adcli-0.9.2/tools/computer.c:702: leaked_storage: Variable "enroll" going out of scope leaks the storage it points to.
#  700|   			if (res != ADCLI_SUCCESS) {
#  701|   				adcli_enroll_unref (enroll);
#  702|-> 				return res;
#  703|   			}
#  704|   			break;', true, 'Non-Issue', '`enroll` is not freed directly in `adcli_unref_enroll()` but in a funtion called in `adcli_unret_enroll()`', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.017309');
INSERT INTO public.ground_truth VALUES (29, 'adcli-0.9.2-6.el10', 29, 'Error: RESOURCE_LEAK (CWE-772):
adcli-0.9.2/library/adkrb5.c:194: alloc_fn: Storage is returned from allocation function "krb5_get_error_message".
adcli-0.9.2/library/adkrb5.c:194: noescape: Resource "krb5_get_error_message(k5, code)" is not freed or pointed-to in "_adcli_err".
adcli-0.9.2/library/adkrb5.c:194: leaked_storage: Failing to save or free storage allocated by "krb5_get_error_message(k5, code)" leaks it.
#  192|   		code = krb5_kt_resolve (k5, keytab_name, keytab);
#  193|   		if (code != 0) {
#  194|-> 			_adcli_err ("Failed to open keytab: %s: %s",
#  195|   			            keytab_name, krb5_get_error_message (k5, code));
#  196|   			return ADCLI_ERR_FAIL;', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-45146

', 'Storage allocated by `krb5_get_error_message` at line 194 is not explicitly freed or stored for later deallocation within the provided code, including within the `_adcli_err` function, leading to a potential memory leak.', '2025-11-18 16:18:56.017309');
INSERT INTO public.ground_truth VALUES (30, 'adcli-0.9.2-6.el10', 30, 'Error: RESOURCE_LEAK (CWE-772):
adcli-0.9.2/tools/computer.c:884: alloc_fn: Storage is returned from allocation function "adcli_enroll_new".
adcli-0.9.2/tools/computer.c:884: var_assign: Assigning: "enroll" = storage returned from "adcli_enroll_new(conn)".
adcli-0.9.2/tools/computer.c:923: noescape: Resource "enroll" is not freed or pointed-to in "adcli_enroll_unref".
adcli-0.9.2/tools/computer.c:924: leaked_storage: Variable "enroll" going out of scope leaks the storage it points to.
#  922|   		       adcli_get_last_error ());
#  923|   		adcli_enroll_unref (enroll);
#  924|-> 		return -res;
#  925|   	}
#  926|', true, 'Non-Issue', '`enroll` is not freed directly in `adcli_unref_enroll()` but in a funtion called in `adcli_unret_enroll()`', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.017309');
INSERT INTO public.ground_truth VALUES (37, 'adcli-0.9.2-6.el10', 37, 'Error: RESOURCE_LEAK (CWE-772):
adcli-0.9.2/tools/computer.c:1170: alloc_fn: Storage is returned from allocation function "adcli_enroll_new".
adcli-0.9.2/tools/computer.c:1170: var_assign: Assigning: "enroll" = storage returned from "adcli_enroll_new(conn)".
adcli-0.9.2/tools/computer.c:1218: noescape: Resource "enroll" is not freed or pointed-to in "adcli_enroll_unref".
adcli-0.9.2/tools/computer.c:1219: leaked_storage: Variable "enroll" going out of scope leaks the storage it points to.
# 1217|   		warnx ("domain name is required");
# 1218|   		adcli_enroll_unref (enroll);
# 1219|-> 		return 2;
# 1220|   	}
# 1221|', true, 'Non-Issue', '`enroll` is not freed directly in `adcli_unref_enroll()` but in a funtion called in `adcli_unret_enroll()`', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.017309');
INSERT INTO public.ground_truth VALUES (31, 'adcli-0.9.2-6.el10', 31, 'Error: RESOURCE_LEAK (CWE-772):
adcli-0.9.2/tools/computer.c:1069: alloc_fn: Storage is returned from allocation function "adcli_enroll_new".
adcli-0.9.2/tools/computer.c:1069: var_assign: Assigning: "enroll" = storage returned from "adcli_enroll_new(conn)".
adcli-0.9.2/tools/computer.c:1110: noescape: Resource "enroll" is not freed or pointed-to in "adcli_enroll_read_computer_account".
adcli-0.9.2/tools/computer.c:1112: noescape: Resource "enroll" is not freed or pointed-to in "adcli_enroll_get_host_fqdn".
adcli-0.9.2/tools/computer.c:1112: noescape: Resource "enroll" is not freed or pointed-to in "adcli_enroll_get_host_fqdn".
adcli-0.9.2/tools/computer.c:1117: noescape: Resource "enroll" is not freed or pointed-to in "adcli_enroll_unref".
adcli-0.9.2/tools/computer.c:1118: leaked_storage: Variable "enroll" going out of scope leaks the storage it points to.
# 1116|   		       adcli_get_last_error ());
# 1117|   		adcli_enroll_unref (enroll);
# 1118|-> 		return -res;
# 1119|   	}
# 1120|', true, 'Non-Issue', '`enroll` is not freed directly in `adcli_unref_enroll()` but in a funtion called in `adcli_unret_enroll()`', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.017309');
INSERT INTO public.ground_truth VALUES (83, 'cpio-2.15-1.el10', 9, 'Error: UNINIT (CWE-457):
cpio-2.15/src/copyin.c:1255: var_decl: Declaring variable "ascii_header" without initializer.
cpio-2.15/src/copyin.c:1263: uninit_use_in_call: Using uninitialized element of array "ascii_header.c_gid" when calling "from_ascii".
# 1261|     file_hdr->c_mode = FROM_HEX (ascii_header.c_mode);
# 1262|     file_hdr->c_uid = FROM_HEX (ascii_header.c_uid);
# 1263|->   file_hdr->c_gid = FROM_HEX (ascii_header.c_gid);
# 1264|     file_hdr->c_nlink = FROM_HEX (ascii_header.c_nlink);
# 1265|     file_hdr->c_mtime = FROM_HEX (ascii_header.c_mtime);', true, 'Non-Issue', 'initialized by the "tape_buffered read"', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.051962');
INSERT INTO public.ground_truth VALUES (32, 'adcli-0.9.2-6.el10', 32, 'Error: RESOURCE_LEAK (CWE-772):
adcli-0.9.2/library/adentry.c:516: alloc_fn: Storage is returned from allocation function "krb5_get_error_message".
adcli-0.9.2/library/adentry.c:516: noescape: Resource "krb5_get_error_message(k5, code)" is not freed or pointed-to in "_adcli_err".
adcli-0.9.2/library/adentry.c:516: leaked_storage: Failing to save or free storage allocated by "krb5_get_error_message(k5, code)" leaks it.
#  514|   
#  515|   	if (code != 0) {
#  516|-> 		_adcli_err ("Couldn''t set password for %s account: %s: %s",
#  517|   		            entry->object_class,
#  518|   		            entry->sam_name, krb5_get_error_message (k5, code));', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-45146

', 'Storage allocated by `krb5_get_error_message(k5, code)` at line 516 is passed to `_adcli_err` without being stored for potential freeing, and no subsequent freeing function is called within the provided code context, confirming a resource leak vulnerability.', '2025-11-18 16:18:56.017309');
INSERT INTO public.ground_truth VALUES (33, 'adcli-0.9.2-6.el10', 33, 'Error: RESOURCE_LEAK (CWE-772):
adcli-0.9.2/tools/computer.c:884: alloc_fn: Storage is returned from allocation function "adcli_enroll_new".
adcli-0.9.2/tools/computer.c:884: var_assign: Assigning: "enroll" = storage returned from "adcli_enroll_new(conn)".
adcli-0.9.2/tools/computer.c:927: noescape: Resource "enroll" is not freed or pointed-to in "parse_fqdn_or_name".
adcli-0.9.2/tools/computer.c:928: noescape: Resource "enroll" is not freed or pointed-to in "adcli_enroll_reset_computer_password".
adcli-0.9.2/tools/computer.c:930: noescape: Resource "enroll" is not freed or pointed-to in "adcli_enroll_password".
adcli-0.9.2/tools/computer.c:935: noescape: Resource "enroll" is not freed or pointed-to in "adcli_enroll_unref".
adcli-0.9.2/tools/computer.c:936: leaked_storage: Variable "enroll" going out of scope leaks the storage it points to.
#  934|   		       adcli_get_last_error ());
#  935|   		adcli_enroll_unref (enroll);
#  936|-> 		return -res;
#  937|   	}
#  938|', true, 'Non-Issue', '`enroll` is not freed directly in `adcli_unref_enroll()` but in a funtion called in `adcli_unret_enroll()`', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.017309');
INSERT INTO public.ground_truth VALUES (34, 'adcli-0.9.2-6.el10', 34, 'Error: RESOURCE_LEAK (CWE-772):
adcli-0.9.2/tools/computer.c:777: alloc_fn: Storage is returned from allocation function "adcli_enroll_new".
adcli-0.9.2/tools/computer.c:777: var_assign: Assigning: "enroll" = storage returned from "adcli_enroll_new(conn)".
adcli-0.9.2/tools/computer.c:820: noescape: Resource "enroll" is not freed or pointed-to in "adcli_enroll_get_computer_password".
adcli-0.9.2/tools/computer.c:832: noescape: Resource "enroll" is not freed or pointed-to in "parse_fqdn_or_name".
adcli-0.9.2/tools/computer.c:837: noescape: Resource "enroll" is not freed or pointed-to in "adcli_enroll_join".
adcli-0.9.2/tools/computer.c:842: noescape: Resource "enroll" is not freed or pointed-to in "adcli_enroll_unref".
adcli-0.9.2/tools/computer.c:843: leaked_storage: Variable "enroll" going out of scope leaks the storage it points to.
#  841|   			       adcli_get_last_error ());
#  842|   			adcli_enroll_unref (enroll);
#  843|-> 			return -res;
#  844|   		}
#  845|', true, 'Non-Issue', '`enroll` is not freed directly in `adcli_unref_enroll()` but in a funtion called in `adcli_unret_enroll()`', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.017309');
INSERT INTO public.ground_truth VALUES (35, 'adcli-0.9.2-6.el10', 35, 'Error: RESOURCE_LEAK (CWE-772):
adcli-0.9.2/tools/computer.c:1170: alloc_fn: Storage is returned from allocation function "adcli_enroll_new".
adcli-0.9.2/tools/computer.c:1170: var_assign: Assigning: "enroll" = storage returned from "adcli_enroll_new(conn)".
adcli-0.9.2/tools/computer.c:1212: noescape: Resource "enroll" is not freed or pointed-to in "adcli_enroll_unref".
adcli-0.9.2/tools/computer.c:1213: leaked_storage: Variable "enroll" going out of scope leaks the storage it points to.
# 1211|   		warnx ("extra arguments specified");
# 1212|   		adcli_enroll_unref (enroll);
# 1213|-> 		return 2;
# 1214|   	}
# 1215|', true, 'Non-Issue', '`enroll` is not freed directly in `adcli_unref_enroll()` but in a funtion called in `adcli_unret_enroll()`', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.017309');
INSERT INTO public.ground_truth VALUES (36, 'adcli-0.9.2-6.el10', 36, 'Error: RESOURCE_LEAK (CWE-772):
adcli-0.9.2/tools/computer.c:443: alloc_fn: Storage is returned from allocation function "adcli_enroll_new".
adcli-0.9.2/tools/computer.c:443: var_assign: Assigning: "enroll" = storage returned from "adcli_enroll_new(conn)".
adcli-0.9.2/tools/computer.c:500: noescape: Resource "enroll" is not freed or pointed-to in "adcli_enroll_unref".
adcli-0.9.2/tools/computer.c:501: leaked_storage: Variable "enroll" going out of scope leaks the storage it points to.
#  499|   		       adcli_get_last_error ());
#  500|   		adcli_enroll_unref (enroll);
#  501|-> 		return -res;
#  502|   	}
#  503|', true, 'Non-Issue', '`enroll` is not freed directly in `adcli_unref_enroll()` but in a funtion called in `adcli_unret_enroll()`', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.017309');
INSERT INTO public.ground_truth VALUES (104, 'cpio-2.15-1.el10', 30, 'Error: UNINIT (CWE-457):
cpio-2.15/gnu/parse-datetime.c:1634: var_decl: Declaring variable "yylval" without initializer.
cpio-2.15/gnu/parse-datetime.c:2643: uninit_use: Using uninitialized value "yylval".
# 2641|   
# 2642|     YY_IGNORE_MAYBE_UNINITIALIZED_BEGIN
# 2643|->   *++yyvsp = yylval;
# 2644|     YY_IGNORE_MAYBE_UNINITIALIZED_END
# 2645|', true, 'Non-Issue', 'YY_IGNORE_MAYBE_UNINITIALIZED_BEGIN', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.051962');
INSERT INTO public.ground_truth VALUES (38, 'adcli-0.9.2-6.el10', 38, 'Error: RESOURCE_LEAK (CWE-772):
adcli-0.9.2/tools/computer.c:972: alloc_fn: Storage is returned from allocation function "adcli_enroll_new".
adcli-0.9.2/tools/computer.c:972: var_assign: Assigning: "enroll" = storage returned from "adcli_enroll_new(conn)".
adcli-0.9.2/tools/computer.c:988: noescape: Resource "enroll" is not freed or pointed-to in "parse_option".
adcli-0.9.2/tools/computer.c:990: noescape: Resource "enroll" is not freed or pointed-to in "adcli_enroll_unref".
adcli-0.9.2/tools/computer.c:991: leaked_storage: Variable "enroll" going out of scope leaks the storage it points to.
#  989|   			if (res != ADCLI_SUCCESS) {
#  990|   				adcli_enroll_unref (enroll);
#  991|-> 				return res;
#  992|   			}
#  993|   			break;', true, 'Non-Issue', '`enroll` is not freed directly in `adcli_unref_enroll()` but in a funtion called in `adcli_unret_enroll()`', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.017309');
INSERT INTO public.ground_truth VALUES (84, 'cpio-2.15-1.el10', 10, 'Error: UNINIT (CWE-457):
cpio-2.15/gnu/time_rz.c:294: var_decl: Declaring variable "tm_1" without initializer.
cpio-2.15/gnu/time_rz.c:310: uninit_use: Using uninitialized value "tm_1". Field "tm_1.tm_gmtoff" is uninitialized.
#  308|             if (revert_tz (old_tz) && ok)
#  309|               {
#  310|->               *tm = tm_1;
#  311|                 return t;
#  312|               }', false, 'Non-Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-44929

', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.051962');
INSERT INTO public.ground_truth VALUES (39, 'adcli-0.9.2-6.el10', 39, 'Error: RESOURCE_LEAK (CWE-772):
adcli-0.9.2/library/adenroll.c:2259: alloc_fn: Storage is returned from allocation function "krb5_get_error_message".
adcli-0.9.2/library/adenroll.c:2259: noescape: Resource "krb5_get_error_message(k5, code)" is not freed or pointed-to in "_adcli_err".
adcli-0.9.2/library/adenroll.c:2259: leaked_storage: Failing to save or free storage allocated by "krb5_get_error_message(k5, code)" leaks it.
# 2257|   
# 2258|   	if (code != 0) {
# 2259|-> 		_adcli_err ("Couldn''t update keytab: %s: %s",
# 2260|   		            enroll->keytab_name, krb5_get_error_message (k5, code));
# 2261|   		return ADCLI_ERR_FAIL;', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-45146

', 'Storage allocated by `krb5_get_error_message(k5, code)` at line 2259 is passed to `_adcli_err` without being stored or freed, and with no visible freeing function call within the provided scope, potentially leading to a RESOURCE_LEAK (CWE-772) when `code != 0`.', '2025-11-18 16:18:56.017309');
INSERT INTO public.ground_truth VALUES (40, 'adcli-0.9.2-6.el10', 40, 'Error: RESOURCE_LEAK (CWE-772):
adcli-0.9.2/library/adenroll.c:2115: alloc_fn: Storage is returned from allocation function "krb5_get_error_message".
adcli-0.9.2/library/adenroll.c:2115: noescape: Resource "krb5_get_error_message(k5, code)" is not freed or pointed-to in "_adcli_err".
adcli-0.9.2/library/adenroll.c:2115: leaked_storage: Failing to save or free storage allocated by "krb5_get_error_message(k5, code)" leaks it.
# 2113|   		code = _adcli_krb5_keytab_enumerate (k5, keytab, load_keytab_entry, enroll);
# 2114|   		if (code != 0) {
# 2115|-> 			_adcli_err ("Couldn''t enumerate keytab: %s: %s",
# 2116|   		                    enroll->keytab_name, krb5_get_error_message (k5, code));
# 2117|   			res = ADCLI_ERR_FAIL;', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-45146

', 'Storage allocated by `krb5_get_error_message(k5, code)` at line 2115 is passed to `_adcli_err` without visible freeing or storage for later freeing in the provided `adenroll.c` code, directly correlating with the RESOURCE_LEAK (CWE-772) vulnerability.', '2025-11-18 16:18:56.017309');
INSERT INTO public.ground_truth VALUES (41, 'adcli-0.9.2-6.el10', 41, 'Error: RESOURCE_LEAK (CWE-772):
adcli-0.9.2/tools/computer.c:1170: alloc_fn: Storage is returned from allocation function "adcli_enroll_new".
adcli-0.9.2/tools/computer.c:1170: var_assign: Assigning: "enroll" = storage returned from "adcli_enroll_new(conn)".
adcli-0.9.2/tools/computer.c:1196: noescape: Resource "enroll" is not freed or pointed-to in "parse_option".
adcli-0.9.2/tools/computer.c:1198: noescape: Resource "enroll" is not freed or pointed-to in "adcli_enroll_unref".
adcli-0.9.2/tools/computer.c:1199: leaked_storage: Variable "enroll" going out of scope leaks the storage it points to.
# 1197|   			if (res != ADCLI_SUCCESS) {
# 1198|   				adcli_enroll_unref (enroll);
# 1199|-> 				return res;
# 1200|   			}
# 1201|   			break;', true, 'Non-Issue', '`enroll` is not freed directly in `adcli_unref_enroll()` but in a funtion called in `adcli_unret_enroll()`', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.017309');
INSERT INTO public.ground_truth VALUES (42, 'adcli-0.9.2-6.el10', 42, 'Error: RESOURCE_LEAK (CWE-772):
adcli-0.9.2/tools/computer.c:443: alloc_fn: Storage is returned from allocation function "adcli_enroll_new".
adcli-0.9.2/tools/computer.c:443: var_assign: Assigning: "enroll" = storage returned from "adcli_enroll_new(conn)".
adcli-0.9.2/tools/computer.c:505: noescape: Resource "enroll" is not freed or pointed-to in "adcli_enroll_set_account_disable".
adcli-0.9.2/tools/computer.c:506: noescape: Resource "enroll" is not freed or pointed-to in "adcli_enroll_join".
adcli-0.9.2/tools/computer.c:511: noescape: Resource "enroll" is not freed or pointed-to in "adcli_enroll_unref".
adcli-0.9.2/tools/computer.c:512: leaked_storage: Variable "enroll" going out of scope leaks the storage it points to.
#  510|   		       adcli_get_last_error ());
#  511|   		adcli_enroll_unref (enroll);
#  512|-> 		return -res;
#  513|   	}
#  514|', true, 'Non-Issue', '`enroll` is not freed directly in `adcli_unref_enroll()` but in a funtion called in `adcli_unret_enroll()`', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.017309');
INSERT INTO public.ground_truth VALUES (43, 'adcli-0.9.2-6.el10', 43, 'Error: RESOURCE_LEAK (CWE-772):
adcli-0.9.2/library/adconn.c:359: alloc_fn: Storage is returned from allocation function "krb5_get_error_message".
adcli-0.9.2/library/adconn.c:359: noescape: Resource "krb5_get_error_message(conn->k5, code)" is not freed or pointed-to in "_adcli_err".
adcli-0.9.2/library/adconn.c:359: leaked_storage: Failing to save or free storage allocated by "krb5_get_error_message(conn->k5, code)" leaks it.
#  357|   			            name, krb5_get_error_message (conn->k5, code));
#  358|   		} else {
#  359|-> 			_adcli_err ("Couldn''t authenticate as: %s: %s",
#  360|   			            name, krb5_get_error_message (conn->k5, code));
#  361|   		}', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-45146

', 'Storage allocated by `krb5_get_error_message` at line 359 is not explicitly freed within the provided code context, including after its use in `_adcli_err` (defined in `adprivate.h` line 107), directly correlating with CWE-772 (Resource Leak).', '2025-11-18 16:18:56.017309');
INSERT INTO public.ground_truth VALUES (59, 'adcli-0.9.2-6.el10', 59, 'Error: RESOURCE_LEAK (CWE-772):
adcli-0.9.2/tools/computer.c:443: alloc_fn: Storage is returned from allocation function "adcli_enroll_new".
adcli-0.9.2/tools/computer.c:443: var_assign: Assigning: "enroll" = storage returned from "adcli_enroll_new(conn)".
adcli-0.9.2/tools/computer.c:475: noescape: Resource "enroll" is not freed or pointed-to in "parse_option".
adcli-0.9.2/tools/computer.c:477: noescape: Resource "enroll" is not freed or pointed-to in "adcli_enroll_unref".
adcli-0.9.2/tools/computer.c:478: leaked_storage: Variable "enroll" going out of scope leaks the storage it points to.
#  476|   			if (res != ADCLI_SUCCESS) {
#  477|   				adcli_enroll_unref (enroll);
#  478|-> 				return res;
#  479|   			}
#  480|   			break;', true, 'Non-Issue', '`enroll` is not freed directly in `adcli_unref_enroll()` but in a funtion called in `adcli_unret_enroll()`', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.017309');
INSERT INTO public.ground_truth VALUES (44, 'adcli-0.9.2-6.el10', 44, 'Error: RESOURCE_LEAK (CWE-772):
adcli-0.9.2/library/adenroll.c:1585: alloc_fn: Storage is returned from allocation function "krb5_get_error_message".
adcli-0.9.2/library/adenroll.c:1585: noescape: Resource "krb5_get_error_message(k5, code)" is not freed or pointed-to in "_adcli_err".
adcli-0.9.2/library/adenroll.c:1585: leaked_storage: Failing to save or free storage allocated by "krb5_get_error_message(k5, code)" leaks it.
# 1583|   	code = _adcli_kinit_computer_creds (enroll->conn, "kadmin/changepw", NULL, &creds);
# 1584|   	if (code != 0) {
# 1585|-> 		_adcli_err ("Couldn''t get change password ticket for %s account: %s: %s",
# 1586|   		            s_or_c (enroll),
# 1587|   		            enroll->computer_sam, krb5_get_error_message (k5, code));', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-45146

', 'Storage allocated by `krb5_get_error_message(k5, code)` at line 1585 is not explicitly freed within the provided code, including within the `_adcli_err` function, indicating a potential resource leak, as no deallocation function (e.g., `krb5_free_error_message`) is visibly called on its return value.', '2025-11-18 16:18:56.017309');
INSERT INTO public.ground_truth VALUES (45, 'adcli-0.9.2-6.el10', 45, 'Error: RESOURCE_LEAK (CWE-772):
adcli-0.9.2/tools/tools.c:402: alloc_arg: "asprintf" allocates memory that is stored into "snippets". [Note: The source code implementation of the function has been overridden by a builtin model.]
adcli-0.9.2/tools/tools.c:402: noescape: Assuming resource "snippets" is not freed or pointed-to as ellipsis argument to "asprintf".
adcli-0.9.2/tools/tools.c:409: overwrite_var: Overwriting "snippets" in "snippets = NULL" leaks the storage that "snippets" points to.
#  407|   				warnx ("unexpected: out of memory");
#  408|   				filename = NULL; /* content is undefined */
#  409|-> 				snippets = NULL; /* content is undefined */
#  410|   				contents = NULL; /* content is undefined */
#  411|   				failed = 1;', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-45146

', 'Memory allocated by `asprintf` for `snippets` (line 402) is not explicitly freed before being overwritten with `NULL` (line 409) in error handling, directly supporting the reported RESOURCE_LEAK (CWE-772) vulnerability.', '2025-11-18 16:18:56.017309');
INSERT INTO public.ground_truth VALUES (46, 'adcli-0.9.2-6.el10', 46, 'Error: RESOURCE_LEAK (CWE-772):
adcli-0.9.2/tools/computer.c:575: alloc_fn: Storage is returned from allocation function "adcli_enroll_new".
adcli-0.9.2/tools/computer.c:575: var_assign: Assigning: "enroll" = storage returned from "adcli_enroll_new(conn)".
adcli-0.9.2/tools/computer.c:603: noescape: Resource "enroll" is not freed or pointed-to in "parse_option".
adcli-0.9.2/tools/computer.c:605: noescape: Resource "enroll" is not freed or pointed-to in "adcli_enroll_unref".
adcli-0.9.2/tools/computer.c:606: leaked_storage: Variable "enroll" going out of scope leaks the storage it points to.
#  604|   			if (res != ADCLI_SUCCESS) {
#  605|   				adcli_enroll_unref (enroll);
#  606|-> 				return res;
#  607|   			}
#  608|   			break;', true, 'Non-Issue', '`enroll` is not freed directly in `adcli_unref_enroll()` but in a funtion called in `adcli_unret_enroll()`', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.017309');
INSERT INTO public.ground_truth VALUES (47, 'adcli-0.9.2-6.el10', 47, 'Error: RESOURCE_LEAK (CWE-772):
adcli-0.9.2/tools/computer.c:777: alloc_fn: Storage is returned from allocation function "adcli_enroll_new".
adcli-0.9.2/tools/computer.c:777: var_assign: Assigning: "enroll" = storage returned from "adcli_enroll_new(conn)".
adcli-0.9.2/tools/computer.c:801: noescape: Resource "enroll" is not freed or pointed-to in "parse_option".
adcli-0.9.2/tools/computer.c:803: noescape: Resource "enroll" is not freed or pointed-to in "adcli_enroll_unref".
adcli-0.9.2/tools/computer.c:804: leaked_storage: Variable "enroll" going out of scope leaks the storage it points to.
#  802|   			if (res != ADCLI_SUCCESS) {
#  803|   				adcli_enroll_unref (enroll);
#  804|-> 				return res;
#  805|   			}
#  806|   			break;', true, 'Non-Issue', '`enroll` is not freed directly in `adcli_unref_enroll()` but in a funtion called in `adcli_unret_enroll()`', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.017309');
INSERT INTO public.ground_truth VALUES (48, 'adcli-0.9.2-6.el10', 48, 'Error: RESOURCE_LEAK (CWE-772):
adcli-0.9.2/library/adkrb5.c:176: alloc_fn: Storage is returned from allocation function "krb5_get_error_message".
adcli-0.9.2/library/adkrb5.c:176: noescape: Resource "krb5_get_error_message(NULL, code)" is not freed or pointed-to in "_adcli_err".
adcli-0.9.2/library/adkrb5.c:176: leaked_storage: Failing to save or free storage allocated by "krb5_get_error_message(NULL, code)" leaks it.
#  174|   
#  175|   	} else if (code != 0) {
#  176|-> 		_adcli_err ("Failed to create kerberos context: %s",
#  177|   		            krb5_get_error_message (NULL, code));
#  178|   		return ADCLI_ERR_UNEXPECTED;', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-45146

', 'Storage allocated by `krb5_get_error_message(NULL, code)` at `adkrb5.c:176` is not explicitly freed within the provided code snippet, particularly in the error path (lines 175-178), directly correlating with CWE-772 (Resource Leak).', '2025-11-18 16:18:56.017309');
INSERT INTO public.ground_truth VALUES (49, 'adcli-0.9.2-6.el10', 49, 'Error: RESOURCE_LEAK (CWE-772):
adcli-0.9.2/tools/computer.c:777: alloc_fn: Storage is returned from allocation function "adcli_enroll_new".
adcli-0.9.2/tools/computer.c:777: var_assign: Assigning: "enroll" = storage returned from "adcli_enroll_new(conn)".
adcli-0.9.2/tools/computer.c:815: noescape: Resource "enroll" is not freed or pointed-to in "adcli_enroll_unref".
adcli-0.9.2/tools/computer.c:816: leaked_storage: Variable "enroll" going out of scope leaks the storage it points to.
#  814|   		warnx ("specify one or more host names of computer accounts to preset");
#  815|   		adcli_enroll_unref (enroll);
#  816|-> 		return EUSAGE;
#  817|   	}
#  818|', true, 'Non-Issue', '`enroll` is not freed directly in `adcli_unref_enroll()` but in a funtion called in `adcli_unret_enroll()`', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.017309');
INSERT INTO public.ground_truth VALUES (50, 'adcli-0.9.2-6.el10', 50, 'Error: RESOURCE_LEAK (CWE-772):
adcli-0.9.2/tools/computer.c:972: alloc_fn: Storage is returned from allocation function "adcli_enroll_new".
adcli-0.9.2/tools/computer.c:972: var_assign: Assigning: "enroll" = storage returned from "adcli_enroll_new(conn)".
adcli-0.9.2/tools/computer.c:1002: noescape: Resource "enroll" is not freed or pointed-to in "adcli_enroll_unref".
adcli-0.9.2/tools/computer.c:1003: leaked_storage: Variable "enroll" going out of scope leaks the storage it points to.
# 1001|   		warnx ("specify one host name of computer account to delete");
# 1002|   		adcli_enroll_unref (enroll);
# 1003|-> 		return EUSAGE;
# 1004|   	}
# 1005|', true, 'Non-Issue', '`enroll` is not freed directly in `adcli_unref_enroll()` but in a funtion called in `adcli_unret_enroll()`', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.017309');
INSERT INTO public.ground_truth VALUES (80, 'cpio-2.15-1.el10', 6, 'Error: UNINIT (CWE-457):
cpio-2.15/src/copyin.c:1255: var_decl: Declaring variable "ascii_header" without initializer.
cpio-2.15/src/copyin.c:1268: uninit_use_in_call: Using uninitialized element of array "ascii_header.c_dev_min" when calling "from_ascii".
# 1266|     file_hdr->c_filesize = FROM_HEX (ascii_header.c_filesize);
# 1267|     file_hdr->c_dev_maj = FROM_HEX (ascii_header.c_dev_maj);
# 1268|->   file_hdr->c_dev_min = FROM_HEX (ascii_header.c_dev_min);
# 1269|     file_hdr->c_rdev_maj = FROM_HEX (ascii_header.c_rdev_maj);
# 1270|     file_hdr->c_rdev_min = FROM_HEX (ascii_header.c_rdev_min);', true, 'Non-Issue', 'initialized by the "tape_buffered read"', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.051962');
INSERT INTO public.ground_truth VALUES (51, 'adcli-0.9.2-6.el10', 51, 'Error: RESOURCE_LEAK (CWE-772):
adcli-0.9.2/tools/tools.c:402: alloc_arg: "asprintf" allocates memory that is stored into "filename". [Note: The source code implementation of the function has been overridden by a builtin model.]
adcli-0.9.2/tools/tools.c:408: overwrite_var: Overwriting "filename" in "filename = NULL" leaks the storage that "filename" points to.
#  406|   			              krb5_conf ? krb5_conf : "") < 0) {
#  407|   				warnx ("unexpected: out of memory");
#  408|-> 				filename = NULL; /* content is undefined */
#  409|   				snippets = NULL; /* content is undefined */
#  410|   				contents = NULL; /* content is undefined */', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-45146

', 'Memory allocated for `filename` at line 402 is overwritten with `NULL` at line 408 without guaranteed deallocation, as the subsequent `free (filename)` at line 458 is conditional and may not execute if `failed` is set to 1, directly linking to a potential memory leak vulnerability.', '2025-11-18 16:18:56.017309');
INSERT INTO public.ground_truth VALUES (52, 'adcli-0.9.2-6.el10', 52, 'Error: RESOURCE_LEAK (CWE-772):
adcli-0.9.2/library/adconn.c:708: alloc_fn: Storage is returned from allocation function "krb5_get_error_message".
adcli-0.9.2/library/adconn.c:708: noescape: Resource "krb5_get_error_message(NULL, code)" is not freed or pointed-to in "_adcli_err".
adcli-0.9.2/library/adconn.c:708: leaked_storage: Failing to save or free storage allocated by "krb5_get_error_message(NULL, code)" leaks it.
#  706|   
#  707|   			if (code != 0) {
#  708|-> 				_adcli_err ("Couldn''t open kerberos credential cache: %s: %s",
#  709|   				            conn->login_ccache_name, krb5_get_error_message (NULL, code));
#  710|   				return ADCLI_ERR_CONFIG;', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-45146

', 'Storage allocated by `krb5_get_error_message(NULL, code)` at line 708 is not freed or pointed-to within the `_adcli_err` function, and the immediate return at line 710 confirms a resource leak, as there''s no visible mechanism for deallocation within the provided code.', '2025-11-18 16:18:56.017309');
INSERT INTO public.ground_truth VALUES (53, 'adcli-0.9.2-6.el10', 53, 'Error: USE_AFTER_FREE (CWE-416):
adcli-0.9.2/library/adutil.c:865: freed_arg: "free" frees "stdout_data".
adcli-0.9.2/library/adutil.c:877: deref_arg: Calling "strncmp" dereferences freed pointer "(char *)stdout_data".
#  875|   	                                    &stdout_data, &stdout_data_len);
#  876|   	assert (res == ADCLI_SUCCESS);
#  877|-> 	assert (strncmp ("olleH\n", (char *) stdout_data, stdout_data_len) == 0);
#  878|   	free (stdout_data);
#  879|   #endif', true, 'Non-Issue', 'After the `free()` in line 865 there is a function call which allocates new memory for `stdout_data`.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.017309');
INSERT INTO public.ground_truth VALUES (54, 'adcli-0.9.2-6.el10', 54, 'Error: RESOURCE_LEAK (CWE-772):
adcli-0.9.2/library/adconn.c:369: alloc_fn: Storage is returned from allocation function "krb5_get_error_message".
adcli-0.9.2/library/adconn.c:369: noescape: Resource "krb5_get_error_message(conn->k5, code)" is not freed or pointed-to in "_adcli_err".
adcli-0.9.2/library/adconn.c:369: leaked_storage: Failing to save or free storage allocated by "krb5_get_error_message(conn->k5, code)" leaks it.
#  367|   			            name, krb5_get_error_message (conn->k5, code));
#  368|   		} else {
#  369|-> 			_adcli_err ("Couldn''t get kerberos ticket for: %s: %s",
#  370|   			            name, krb5_get_error_message (conn->k5, code));
#  371|   		}', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-45146

', 'Storage allocated by `krb5_get_error_message` is not explicitly freed within the provided code snippet (lines 367-371), potentially leading to a resource leak (CWE-772), as it is not freed or pointed-to after allocation in both conditional branches.', '2025-11-18 16:18:56.017309');
INSERT INTO public.ground_truth VALUES (55, 'adcli-0.9.2-6.el10', 55, 'Error: RESOURCE_LEAK (CWE-772):
adcli-0.9.2/tools/computer.c:777: alloc_fn: Storage is returned from allocation function "adcli_enroll_new".
adcli-0.9.2/tools/computer.c:777: var_assign: Assigning: "enroll" = storage returned from "adcli_enroll_new(conn)".
adcli-0.9.2/tools/computer.c:792: noescape: Resource "enroll" is not freed or pointed-to in "adcli_enroll_unref".
adcli-0.9.2/tools/computer.c:793: leaked_storage: Variable "enroll" going out of scope leaks the storage it points to.
#  791|   			adcli_tool_usage (options, common_usages);
#  792|   			adcli_enroll_unref (enroll);
#  793|-> 			return 0;
#  794|   		case ''?'':
#  795|   		case '':'':', true, 'Non-Issue', '`enroll` is not freed directly in `adcli_unref_enroll()` but in a funtion called in `adcli_unret_enroll()`', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.017309');
INSERT INTO public.ground_truth VALUES (56, 'adcli-0.9.2-6.el10', 56, 'Error: RESOURCE_LEAK (CWE-772):
adcli-0.9.2/library/seq.c:316: alloc_fn: Storage is returned from allocation function "calloc".
adcli-0.9.2/library/seq.c:316: var_assign: Assigning: "copied" = storage returned from "calloc(alloc, 8UL)".
adcli-0.9.2/library/seq.c:324: leaked_storage: Variable "copied" going out of scope leaks the storage it points to.
#  322|   		} else {
#  323|   			copied[at] = copy (seq[at]);
#  324|-> 			bail_on_null (copied[at]);
#  325|   		}
#  326|   	}', false, 'Non-Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-45146

', 'Memory allocated for ''copied'' is returned at line 329, transferring ownership to the caller, and there''s no evidence within the provided code that this memory is mishandled, thus no leak within this function''s scope.', '2025-11-18 16:18:56.017309');
INSERT INTO public.ground_truth VALUES (57, 'adcli-0.9.2-6.el10', 57, 'Error: RESOURCE_LEAK (CWE-772):
adcli-0.9.2/tools/computer.c:1170: alloc_fn: Storage is returned from allocation function "adcli_enroll_new".
adcli-0.9.2/tools/computer.c:1170: var_assign: Assigning: "enroll" = storage returned from "adcli_enroll_new(conn)".
adcli-0.9.2/tools/computer.c:1222: noescape: Resource "enroll" is not freed or pointed-to in "adcli_enroll_set_is_service".
adcli-0.9.2/tools/computer.c:1224: noescape: Resource "enroll" is not freed or pointed-to in "adcli_enroll_add_keytab_for_service_account".
adcli-0.9.2/tools/computer.c:1227: noescape: Resource "enroll" is not freed or pointed-to in "adcli_enroll_unref".
adcli-0.9.2/tools/computer.c:1228: leaked_storage: Variable "enroll" going out of scope leaks the storage it points to.
# 1226|   		warnx ("Failed to set domain specific keytab name");
# 1227|   		adcli_enroll_unref (enroll);
# 1228|-> 		return 2;
# 1229|   	}
# 1230|', true, 'Non-Issue', '`enroll` is not freed directly in `adcli_unref_enroll()` but in a funtion called in `adcli_unret_enroll()`', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.017309');
INSERT INTO public.ground_truth VALUES (58, 'adcli-0.9.2-6.el10', 58, 'Error: RESOURCE_LEAK (CWE-772):
adcli-0.9.2/tools/computer.c:1069: alloc_fn: Storage is returned from allocation function "adcli_enroll_new".
adcli-0.9.2/tools/computer.c:1069: var_assign: Assigning: "enroll" = storage returned from "adcli_enroll_new(conn)".
adcli-0.9.2/tools/computer.c:1102: noescape: Resource "enroll" is not freed or pointed-to in "adcli_enroll_unref".
adcli-0.9.2/tools/computer.c:1103: leaked_storage: Variable "enroll" going out of scope leaks the storage it points to.
# 1101|   		       adcli_get_last_error ());
# 1102|   		adcli_enroll_unref (enroll);
# 1103|-> 		return -res;
# 1104|   	}
# 1105|', true, 'Non-Issue', '`enroll` is not freed directly in `adcli_unref_enroll()` but in a funtion called in `adcli_unret_enroll()`', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.017309');
INSERT INTO public.ground_truth VALUES (60, 'adcli-0.9.2-6.el10', 60, 'Error: RESOURCE_LEAK (CWE-772):
adcli-0.9.2/tools/computer.c:884: alloc_fn: Storage is returned from allocation function "adcli_enroll_new".
adcli-0.9.2/tools/computer.c:884: var_assign: Assigning: "enroll" = storage returned from "adcli_enroll_new(conn)".
adcli-0.9.2/tools/computer.c:897: noescape: Resource "enroll" is not freed or pointed-to in "adcli_enroll_unref".
adcli-0.9.2/tools/computer.c:898: leaked_storage: Variable "enroll" going out of scope leaks the storage it points to.
#  896|   			adcli_tool_usage (options, common_usages);
#  897|   			adcli_enroll_unref (enroll);
#  898|-> 			return opt == ''h'' ? 0 : 2;
#  899|   		default:
#  900|   			res = parse_option ((Option)opt, optarg, conn, enroll);', true, 'Non-Issue', '`enroll` is not freed directly in `adcli_unref_enroll()` but in a funtion called in `adcli_unret_enroll()`', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.017309');
INSERT INTO public.ground_truth VALUES (61, 'audit-4.0-8.el10', 1, 'Error: OVERRUN (CWE-119):
audit-4.0/auparse/auparse.c:514: strlen_assign: Setting variable "len" to the return value of strlen called with argument "buf".
audit-4.0/auparse/auparse.c:515: alloc_strlen: Allocating insufficient memory for the terminating null of the string.
#  513|   				goto bad_exit;
#  514|   			len = strlen(buf);
#  515|-> 			if (databuf_init(&au->databuf, len,
#  516|   					 DATABUF_FLAG_PRESERVE_HEAD) < 0)
#  517|   				goto bad_exit;', true, 'Non-Issue', 'Data inside databuf does not need to be null-terminated because the length is tracked internally, memory copy/move operations are performed based on the actual length. ', 'Insufficient memory is allocated for null-termination at line 515 (`databuf_init(&au->databuf, len, ...)`), directly matching the ''alloc_strlen'' warning, as `databuf_init` (data_buf.c, lines 156-175) does not automatically account for the null-terminator, potentially leading to a buffer overflow vulnerability.', '2025-11-18 16:18:56.040141');
INSERT INTO public.ground_truth VALUES (62, 'audit-4.0-8.el10', 2, 'Error: INTEGER_OVERFLOW (CWE-190):
audit-4.0/src/auditd-listen.c:595: tainted_data_return: Called function "read(io->io.fd, io->buffer + io->bufptr, 8970U - io->bufptr)", and a possible return value may be less than zero.
audit-4.0/src/auditd-listen.c:595: cast_overflow: An assign that casts to a different type, which might trigger an overflow.
audit-4.0/src/auditd-listen.c:637: overflow: The expression "io->bufptr" is considered to have possibly overflowed.
audit-4.0/src/auditd-listen.c:735: overflow: The expression "io->bufptr -= i" is deemed overflowed because at least one of its arguments has overflowed.
audit-4.0/src/auditd-listen.c:739: cast_overflow: An assign that casts to a different type, which might trigger an overflow.
audit-4.0/src/auditd-listen.c:637: overflow: The expression "io->bufptr += r" is deemed underflowed because at least one of its arguments has underflowed.
audit-4.0/src/auditd-listen.c:735: overflow: The expression "io->bufptr -= i" is deemed underflowed because at least one of its arguments has underflowed.
audit-4.0/src/auditd-listen.c:595: overflow: The expression "8970U - io->bufptr" is deemed underflowed because at least one of its arguments has underflowed.
audit-4.0/src/auditd-listen.c:595: overflow_sink: "8970U - io->bufptr", which might have underflowed, is passed to "read(io->io.fd, io->buffer + io->bufptr, 8970U - io->bufptr)". [Note: The source code implementation of the function has been overridden by a builtin model.]
#  593|   	   data.  */
#  594|   read_more:
#  595|-> 	r = read (io->io.fd,
#  596|   		  io->buffer + io->bufptr,
#  597|   		  MAX_AUDIT_MESSAGE_LENGTH - io->bufptr);', true, 'Non-Issue', 'If the return value of read() is less than zero, it is handled appropriately, ensuring that it will not be negative if read() is called again', 'Potential negative return values from `read()` are not fully handled (lines 599-600), and unvalidated `io->bufptr` calculations (e.g., line 597) may lead to underflows, directly correlating with the CVE''s described vulnerabilities and lacking explicit safety checks.', '2025-11-18 16:18:56.040141');
INSERT INTO public.ground_truth VALUES (63, 'audit-4.0-8.el10', 3, 'Error: INTEGER_OVERFLOW (CWE-190):
audit-4.0/src/auditd-listen.c:171: tainted_data_return: Called function "write(sock, buf, len)", and a possible return value may be less than zero.
audit-4.0/src/auditd-listen.c:171: cast_overflow: An assign that casts to a different type, which might trigger an overflow.
audit-4.0/src/auditd-listen.c:178: overflow: The expression "len" is considered to have possibly overflowed.
audit-4.0/src/auditd-listen.c:171: overflow_sink: "len", which might have overflowed, is passed to "write(sock, buf, len)".
#  169|           while (len > 0) {
#  170|                   do {
#  171|->                         w = write(sock, buf, len);
#  172|                   } while (w < 0 && errno == EINTR);
#  173|                   if (w < 0)', true, 'Non-Issue', 'If the return value of write() is less than zero, it is handled appropriately, ensuring that it will not be negative if write() is called again', 'The investigation yields a TRUE POSITIVE due to potential vulnerabilities at line 171, notably: (1) unmitigated negative return values from `write()`, (2) possible cast overflow in assigning `write()`''s return value to `w` (type uncertainty), and (3) unchecked ''len'' overflow risks in decrement operations and its subsequent pass to `write()`, all directly correlating with the CVE''s described vulnerabilities.', '2025-11-18 16:18:56.040141');
INSERT INTO public.ground_truth VALUES (64, 'audit-4.0-8.el10', 4, 'Error: INTEGER_OVERFLOW (CWE-190):
audit-4.0/lib/libaudit.c:961: tainted_data_return: Called function "write(o, &loginuid[offset], (unsigned int)count)", and a possible return value may be less than zero.
audit-4.0/lib/libaudit.c:961: cast_overflow: An assign that casts to a different type, which might trigger an overflow.
audit-4.0/lib/libaudit.c:971: overflow: The expression "count" is considered to have possibly overflowed.
audit-4.0/lib/libaudit.c:961: overflow_sink: "(unsigned int)count", which might have overflowed, is passed to "write(o, &loginuid[offset], (unsigned int)count)".
#  959|   
#  960|                   while (count > 0) {
#  961|->                         block = write(o, &loginuid[offset], (unsigned)count);
#  962|   
#  963|                           if (block < 0) {', true, 'Issue', 'By converting an unsigned integer and storing it in a char[16] array, the resulting string will always fit within 16 characters due to the maximum size of an unsigned integer.', 'The code is vulnerable due to a potential negative return value from `write()` not fully mitigated by error checking (line 963), coupled with a cast overflow risk when converting `count` to `(unsigned int)` (line 961), which could mask errors or overflows originating from `snprintf()`''s return value (line 955).', '2025-11-18 16:18:56.040141');
INSERT INTO public.ground_truth VALUES (65, 'audit-4.0-8.el10', 5, 'Error: USE_AFTER_FREE (CWE-416):
audit-4.0/src/auditd-event.c:1487: alias: Equality between "oconf->plugin_dir" and "nconf->plugin_dir" implies that they are aliases.
audit-4.0/src/auditd-event.c:1490: freed_arg: "free" frees "oconf->plugin_dir".
audit-4.0/src/auditd-event.c:1491: use_after_free: Using freed pointer "nconf->plugin_dir".
# 1489|                   strcmp(oconf->plugin_dir, nconf->plugin_dir) != 0)) {
# 1490|                   free(oconf->plugin_dir);
# 1491|->                 oconf->plugin_dir = nconf->plugin_dir;
# 1492|           }
# 1493|', true, 'Non-Issue', 'oconf->plugin_dir and nconf->plugin_dir are never aliases. Both are malloced and initialized at the beginning of their lifetime.', 'Direct use after free at line 1491, where `oconf->plugin_dir` is assigned the potentially freed `nconf->plugin_dir` (line 1490), especially under alias conditions (line 1487), identifies a vulnerable execution path.', '2025-11-18 16:18:56.040141');
INSERT INTO public.ground_truth VALUES (66, 'audit-4.0-8.el10', 6, 'Error: CPPCHECK_WARNING (CWE-401):
audit-4.0/src/ausearch-parse.c:1737: error[memleak]: Memory leak: sn.str
# 1735|   							strdup(un->sun_path+1);
# 1736|   						else
# 1737|-> 							return 6;
# 1738|   
# 1739|   						sn.key = NULL;', true, 'Non-Issue', 'There is malloc called for sn.str at all, thus no memory to free.', 'Memory allocated for `sn.str` via `strdup(un->sun_path+1)` at line 1735 is not freed before the function returns 6 at line 1737, directly causing a memory leak when the `else` condition is met.', '2025-11-18 16:18:56.040141');
INSERT INTO public.ground_truth VALUES (67, 'audit-4.0-8.el10', 7, 'Error: INTEGER_OVERFLOW (CWE-190):
audit-4.0/src/auditd-listen.c:190: tainted_data_return: Called function "read(sock, buf, len)", and a possible return value may be less than zero.
audit-4.0/src/auditd-listen.c:190: cast_overflow: An assign that casts to a different type, which might trigger an overflow.
audit-4.0/src/auditd-listen.c:197: overflow: The expression "len" is considered to have possibly overflowed.
audit-4.0/src/auditd-listen.c:190: overflow_sink: "len", which might have overflowed, is passed to "read(sock, buf, len)". [Note: The source code implementation of the function has been overridden by a builtin model.]
#  188|   	while (len > 0) {
#  189|   		do {
#  190|-> 			r = read(sock, buf, len);
#  191|   		} while (r < 0 && errno == EINTR);
#  192|   		if (r < 0)', true, 'Non-Issue', 'If the return value of read() is less than zero, it is handled appropriately, ensuring that it will not be negative if read() is called again', 'The investigation result is supported by multiple execution paths potentially leading to vulnerabilities: `read()`''s return value can indicate errors without range validation (line 190), `len` subtraction may overflow (line 197), and a potentially overflowed `len` is passed to `read()` (line 190), with no explicit overflow checks in the loop.', '2025-11-18 16:18:56.040141');
INSERT INTO public.ground_truth VALUES (85, 'cpio-2.15-1.el10', 11, 'Error: UNINIT (CWE-457):
cpio-2.15/src/copyin.c:1196: var_decl: Declaring variable "ascii_header" without initializer.
cpio-2.15/src/copyin.c:1205: uninit_use_in_call: Using uninitialized element of array "ascii_header.c_ino" when calling "from_ascii".
# 1203|     file_hdr->c_dev_min = minor (dev);
# 1204|   
# 1205|->   file_hdr->c_ino = FROM_OCTAL (ascii_header.c_ino);
# 1206|     file_hdr->c_mode = FROM_OCTAL (ascii_header.c_mode);
# 1207|     file_hdr->c_uid = FROM_OCTAL (ascii_header.c_uid);', true, 'Non-Issue', '
initialized by the "tape_buffered read"', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.051962');
INSERT INTO public.ground_truth VALUES (467, 'texinfo-7.1-2.el10', 22, 'Error: UNINIT (CWE-457):
texinfo-7.1/info/session.c:2926: var_decl: Declaring variable "description" without initializer.
texinfo-7.1/info/session.c:2936: uninit_use: Using uninitialized value "description".
# 2934|       description = window->node->prev;
# 2935|   
# 2936|->   if (!description)
# 2937|       {
# 2938|         info_error (msg_no_pointer, label);', false, 'Non-Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-43595

', NULL, '2025-11-18 16:18:56.203498');
INSERT INTO public.ground_truth VALUES (68, 'audit-4.0-8.el10', 8, 'Error: OVERRUN (CWE-119):
audit-4.0/tools/aulast/aulast.c:286: alias: Assigning: "term" = ""?"". "term" now points to byte 0 of ""?"" (which consists of 2 bytes).
audit-4.0/tools/aulast/aulast.c:350: alias: Assigning: "n.term" = "term". "n.term" now points to byte 0 of ""?"" (which consists of 2 bytes).
audit-4.0/tools/aulast/aulast.c:357: overrun-buffer-val: Overrunning buffer pointed to by "n.term" of 2 bytes by passing it to a function which accesses it at byte offset 4.
#  355|   		n.user_login_proof = auparse_get_serial(au);
#  356|   		n.user_end_proof = 0;
#  357|-> 		report_session(&n); 
#  358|   	} else if (debug)
#  359|   		printf("Session not found or updated\n");', true, 'Non-Issue', 'If n.term has a length of 2 bytes, it will not be accessed at byte offset 4. The strncmp function will not compare beyond 2 bytes even if the third argument is set to 5.', 'Assignment of a 2-byte string to `term` (aulast.c:286) and its propagation to `n.term` (aulast.c:350), followed by passing `n.term` to `report_session` (aulast.c:357) without evident in-code protections against buffer overruns for short inputs, lacks definitive proof to rule out a buffer overrun vulnerability.', '2025-11-18 16:18:56.040141');
INSERT INTO public.ground_truth VALUES (69, 'audit-4.0-8.el10', 9, 'Error: INTEGER_OVERFLOW (CWE-125):
audit-4.0/lib/libaudit.c:961: tainted_data_return: Called function "write(o, &loginuid[offset], (unsigned int)count)", and a possible return value may be less than zero.
audit-4.0/lib/libaudit.c:961: cast_overflow: An assign that casts to a different type, which might trigger an overflow.
audit-4.0/lib/libaudit.c:970: overflow: The expression "offset" is considered to have possibly overflowed.
audit-4.0/lib/libaudit.c:961: deref_overflow: "offset", which might have overflowed, is used in a pointer index in "loginuid[offset]".
#  959|   
#  960|                   while (count > 0) {
#  961|->                         block = write(o, &loginuid[offset], (unsigned)count);
#  962|   
#  963|                           if (block < 0) {', true, 'Non-Issue', 'By converting an unsigned integer and storing it in a char[16] array, the resulting string will always fit within 16 characters due to the maximum size of an unsigned integer.', 'The code is vulnerable due to potential overflow in `offset` (line 970) when indexing `loginuid` of fixed size 16, exacerbated by the handling of `write()`''s return value (line 961) and lack of explicit safeguards against such overflows.', '2025-11-18 16:18:56.040141');
INSERT INTO public.ground_truth VALUES (70, 'audit-4.0-8.el10', 10, 'Error: INTEGER_OVERFLOW (CWE-190):
audit-4.0/src/auditd-listen.c:595: tainted_data_return: Called function "read(io->io.fd, io->buffer + io->bufptr, 8970U - io->bufptr)", and a possible return value may be less than zero.
audit-4.0/src/auditd-listen.c:595: cast_overflow: An assign that casts to a different type, which might trigger an overflow.
audit-4.0/src/auditd-listen.c:637: overflow: The expression "io->bufptr" is considered to have possibly overflowed.
audit-4.0/src/auditd-listen.c:734: overflow: The expression "io->bufptr - i" is deemed overflowed because at least one of its arguments has overflowed.
audit-4.0/src/auditd-listen.c:734: overflow_sink: "io->bufptr - i", which might have underflowed, is passed to "memmove(io->buffer, io->buffer + i, io->bufptr - i)". [Note: The source code implementation of the function has been overridden by a builtin model.]
#  732|   	/* Now copy any remaining bytes to the beginning of the
#  733|   	   buffer.  */
#  734|-> 	memmove(io->buffer, io->buffer + i, io->bufptr - i);
#  735|   	io->bufptr -= i;
#  736|', true, 'Non-Issue', 'If the return value of read() is less than zero, it is handled appropriately, ensuring that it will not be negative if read() is called again. Thus, memmove() will not receive an underflowed arg.', 'Potential buffer overflow/underflow exists due to unchecked `read()` return values (Line 595) and unverified `io->bufptr` values approaching/exceeding `8970U`, which are used in sensitive memory operations like `memmove()` (Line 734), with insufficient error/overflow checks to guarantee safety.', '2025-11-18 16:18:56.040141');
INSERT INTO public.ground_truth VALUES (71, 'audit-4.0-8.el10', 11, 'Error: USE_AFTER_FREE (CWE-416):
audit-4.0/auparse/lru.c:120: freed_arg: "dequeue" frees "queue->end".
audit-4.0/auparse/lru.c:120: deref_arg: Calling "dequeue" dereferences freed pointer "queue->end".
#  118|   
#  119|           while (queue->count)
#  120|->                 dequeue(queue);
#  121|   
#  122|           free(queue);', true, 'Non-Issue', 'The dequeue function will not dereference a freed pointer. We loop while queue->count is greater than 0, removing the end node from the queue in each iteration. Additionally, there is an extra check to verify that the queue is not empty.', 'The ''dequeue'' function frees ''queue->end'' (line 268) without guaranteeing it won''t be dereferenced again, as evidenced by potential subsequent accesses within the while loop (line 119) before ''queue->end'' is updated, directly correlating with the CVE''s ''freed_arg'' and ''deref_arg'' warnings.', '2025-11-18 16:18:56.040141');
INSERT INTO public.ground_truth VALUES (72, 'audit-4.0-8.el10', 12, 'Error: CPPCHECK_WARNING (CWE-401):
audit-4.0/auparse/ellist.c:316: error[memleak]: Memory leak: n.name
#  314|                                                                            == 0)
#  315|                                                                   free(buf);
#  316|->                                                         return -1;
#  317|                                                   }
#  318|                                                   if (tmpctx[0]) {', true, 'Non-Issue', 'No memory is allocated for .name variable, thuse we don''t need to free anything here.', 'Memory leak possible at line 316 for `n.name` due to direct return without freeing, following allocations via `strdup` (e.g., lines 191, 217, 231, 302, 328), and lack of explicit deallocation in the error handling path.', '2025-11-18 16:18:56.040141');
INSERT INTO public.ground_truth VALUES (73, 'audit-4.0-8.el10', 13, 'Error: OVERRUN (CWE-119):
audit-4.0/lib/netlink.c:219: cond_at_least: Checking "size" implies that "size" is at least 1 on the true branch.
audit-4.0/lib/netlink.c:220: overrun-buffer-arg: Overrunning struct type nlmsghdr of 16 bytes by passing it to a function which accesses it at byte offset 16 using argument "size" (which evaluates to 1). [Note: The source code implementation of the function has been overridden by a builtin model.]
#  218|   	req.nlh.nlmsg_seq = sequence;
#  219|   	if (size && data)
#  220|-> 		memcpy(NLMSG_DATA(&req.nlh), data, size);
#  221|   	memset(&addr, 0, sizeof(addr));
#  222|   	addr.nl_family = AF_NETLINK;', false, 'Non-Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-42443

', 'No explicit bounds check ensures `size` doesn''t exceed the remaining buffer space after the 16-byte `nlmsghdr` header, potentially triggering a buffer overrun at line 220, despite the `NLMSG_SPACE(size)` check for total message length.', '2025-11-18 16:18:56.040141');
INSERT INTO public.ground_truth VALUES (86, 'cpio-2.15-1.el10', 12, 'Error: UNINIT (CWE-457):
cpio-2.15/src/copyin.c:1255: var_decl: Declaring variable "ascii_header" without initializer.
cpio-2.15/src/copyin.c:1269: uninit_use_in_call: Using uninitialized element of array "ascii_header.c_rdev_maj" when calling "from_ascii".
# 1267|     file_hdr->c_dev_maj = FROM_HEX (ascii_header.c_dev_maj);
# 1268|     file_hdr->c_dev_min = FROM_HEX (ascii_header.c_dev_min);
# 1269|->   file_hdr->c_rdev_maj = FROM_HEX (ascii_header.c_rdev_maj);
# 1270|     file_hdr->c_rdev_min = FROM_HEX (ascii_header.c_rdev_min);
# 1271|     file_hdr->c_chksum = FROM_HEX (ascii_header.c_chksum);', true, 'Non-Issue', '
initialized by the "tape_buffered read"', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.051962');
INSERT INTO public.ground_truth VALUES (74, 'audit-4.0-8.el10', 14, 'Error: INTEGER_OVERFLOW (CWE-190):
audit-4.0/audisp/plugins/remote/audisp-remote.c:1238: tainted_data_return: Called function "write(sk, buf, len)", and a possible return value may be less than zero.
audit-4.0/audisp/plugins/remote/audisp-remote.c:1238: cast_overflow: An assign that casts to a different type, which might trigger an overflow.
audit-4.0/audisp/plugins/remote/audisp-remote.c:1249: overflow: The expression "len" is considered to have possibly overflowed.
audit-4.0/audisp/plugins/remote/audisp-remote.c:1238: overflow_sink: "len", which might have overflowed, is passed to "write(sk, buf, len)".
# 1236|           while (len > 0) {
# 1237|                   do {
# 1238|->                         r = write(sk, buf, len);
# 1239|                   } while (r < 0 && errno == EINTR);
# 1240|                   if (r < 0) {', true, 'Non-Issue', 'If the return value of write() is less than zero, it is handled appropriately, ensuring that it will not be negative if write() is called again', 'The investigation result is supported by potential vulnerabilities at lines 1238 and 1249, including a possible negative return value from `write()`, a risk of cast overflow due to uncertain variable types, and a potential overflow in the `len` expression, which are not definitively mitigated within the provided code snippet.', '2025-11-18 16:18:56.040141');
INSERT INTO public.ground_truth VALUES (75, 'cpio-2.15-1.el10', 1, 'Error: UNINIT (CWE-457):
cpio-2.15/src/copyin.c:1255: var_decl: Declaring variable "ascii_header" without initializer.
cpio-2.15/src/copyin.c:1270: uninit_use_in_call: Using uninitialized element of array "ascii_header.c_rdev_min" when calling "from_ascii".
# 1268|     file_hdr->c_dev_min = FROM_HEX (ascii_header.c_dev_min);
# 1269|     file_hdr->c_rdev_maj = FROM_HEX (ascii_header.c_rdev_maj);
# 1270|->   file_hdr->c_rdev_min = FROM_HEX (ascii_header.c_rdev_min);
# 1271|     file_hdr->c_chksum = FROM_HEX (ascii_header.c_chksum);
# 1272|     read_name_from_file (file_hdr, in_des, FROM_HEX (ascii_header.c_namesize));', true, 'Non-Issue', 'it was initialized by "tape_buffered_read (ascii_header.c_ino, in_des, sizeof ascii_header - sizeof ascii_header.c_magic);"', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.051962');
INSERT INTO public.ground_truth VALUES (76, 'cpio-2.15-1.el10', 2, 'Error: UNINIT (CWE-457):
cpio-2.15/src/copyin.c:1196: var_decl: Declaring variable "ascii_header" without initializer.
cpio-2.15/src/copyin.c:1210: uninit_use_in_call: Using uninitialized element of array "ascii_header.c_rdev" when calling "from_ascii".
# 1208|     file_hdr->c_gid = FROM_OCTAL (ascii_header.c_gid);
# 1209|     file_hdr->c_nlink = FROM_OCTAL (ascii_header.c_nlink);
# 1210|->   dev = FROM_OCTAL (ascii_header.c_rdev);
# 1211|     file_hdr->c_rdev_maj = major (dev);
# 1212|     file_hdr->c_rdev_min = minor (dev);', true, 'Non-Issue', 'it was initialized by "tape_buffered_read (ascii_header.c_dev, in_des, sizeof ascii_header - sizeof ascii_header.c_magic);"', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.051962');
INSERT INTO public.ground_truth VALUES (77, 'cpio-2.15-1.el10', 3, 'Error: UNINIT (CWE-457):
cpio-2.15/src/util.c:1343: var_decl: Declaring variable "fs" without initializer.
cpio-2.15/src/util.c:1347: uninit_use_in_call: Using uninitialized value "fs". Field "fs.c_magic" is uninitialized when calling "delay_cpio_set_stat".
# 1345|     stat_to_cpio (&fs, st);
# 1346|     fs.c_name = (char*) file_name;
# 1347|->   delay_cpio_set_stat (&fs, invert_permissions);
# 1348|   }
# 1349|', true, 'Non-Issue', 'initialized on line 1345', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.051962');
INSERT INTO public.ground_truth VALUES (78, 'cpio-2.15-1.el10', 4, 'Error: UNINIT (CWE-457):
cpio-2.15/src/copyin.c:1196: var_decl: Declaring variable "ascii_header" without initializer.
cpio-2.15/src/copyin.c:1206: uninit_use_in_call: Using uninitialized element of array "ascii_header.c_mode" when calling "from_ascii".
# 1204|   
# 1205|     file_hdr->c_ino = FROM_OCTAL (ascii_header.c_ino);
# 1206|->   file_hdr->c_mode = FROM_OCTAL (ascii_header.c_mode);
# 1207|     file_hdr->c_uid = FROM_OCTAL (ascii_header.c_uid);
# 1208|     file_hdr->c_gid = FROM_OCTAL (ascii_header.c_gid);', true, 'Non-Issue', 'initialized by the "tape_buffered read"', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.051962');
INSERT INTO public.ground_truth VALUES (79, 'cpio-2.15-1.el10', 5, 'Error: UNINIT (CWE-457):
cpio-2.15/src/copyin.c:1196: var_decl: Declaring variable "ascii_header" without initializer.
cpio-2.15/src/copyin.c:1214: uninit_use_in_call: Using uninitialized element of array "ascii_header.c_mtime" when calling "from_ascii".
# 1212|     file_hdr->c_rdev_min = minor (dev);
# 1213|   
# 1214|->   file_hdr->c_mtime = FROM_OCTAL (ascii_header.c_mtime);
# 1215|     file_hdr->c_filesize = FROM_OCTAL (ascii_header.c_filesize);
# 1216|     read_name_from_file (file_hdr, in_des, FROM_OCTAL (ascii_header.c_namesize));', true, 'Non-Issue', 'initialized by the "tape_buffered read"', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.051962');
INSERT INTO public.ground_truth VALUES (87, 'cpio-2.15-1.el10', 13, 'Error: BUFFER_SIZE (CWE-170):
cpio-2.15/src/tar.c:203: buffer_size_warning: Calling "strncpy" with a maximum size argument of 100 bytes on destination array "tar_hdr->linkname" of size 100 bytes might leave the destination string unterminated.
#  201|         /* process_copy_out makes sure that c_tar_linkname is shorter
#  202|   	 than TARLINKNAMESIZE.  */
#  203|->       strncpy (tar_hdr->linkname, file_hdr->c_tar_linkname,
#  204|   	       TARLINKNAMESIZE);
#  205|         to_ascii (tar_hdr->size, 0, 12, LG_8, true);', false, 'Non-Issue', 'possibly -- easy fix to change TARLINKNAMESIZE to TARLINKNAMESIZE-1 in the strncpy', 'The `strncpy` call on line 203 is guaranteed to leave space for null-termination since `process_copy_out` ensures `c_tar_linkname` is shorter than `TARLINKNAMESIZE` (line 201), matching the destination array `tar_hdr->linkname` size of 100 bytes.', '2025-11-18 16:18:56.051962');
INSERT INTO public.ground_truth VALUES (88, 'cpio-2.15-1.el10', 14, 'Error: UNINIT (CWE-457):
cpio-2.15/src/copyin.c:1255: var_decl: Declaring variable "ascii_header" without initializer.
cpio-2.15/src/copyin.c:1272: uninit_use_in_call: Using uninitialized element of array "ascii_header.c_namesize" when calling "from_ascii".
# 1270|     file_hdr->c_rdev_min = FROM_HEX (ascii_header.c_rdev_min);
# 1271|     file_hdr->c_chksum = FROM_HEX (ascii_header.c_chksum);
# 1272|->   read_name_from_file (file_hdr, in_des, FROM_HEX (ascii_header.c_namesize));
# 1273|   
# 1274|     /* In SVR4 ASCII format, the amount of space allocated for the header', true, 'Non-Issue', 'initialized by the "tape_buffered read"', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.051962');
INSERT INTO public.ground_truth VALUES (89, 'cpio-2.15-1.el10', 15, 'Error: UNINIT (CWE-457):
cpio-2.15/src/copyin.c:1196: var_decl: Declaring variable "ascii_header" without initializer.
cpio-2.15/src/copyin.c:1215: uninit_use_in_call: Using uninitialized element of array "ascii_header.c_filesize" when calling "from_ascii".
# 1213|   
# 1214|     file_hdr->c_mtime = FROM_OCTAL (ascii_header.c_mtime);
# 1215|->   file_hdr->c_filesize = FROM_OCTAL (ascii_header.c_filesize);
# 1216|     read_name_from_file (file_hdr, in_des, FROM_OCTAL (ascii_header.c_namesize));
# 1217|', true, 'Non-Issue', 'initialized by the "tape_buffered read"', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.051962');
INSERT INTO public.ground_truth VALUES (90, 'cpio-2.15-1.el10', 16, 'Error: UNINIT (CWE-457):
cpio-2.15/src/copyin.c:1255: var_decl: Declaring variable "ascii_header" without initializer.
cpio-2.15/src/copyin.c:1261: uninit_use_in_call: Using uninitialized element of array "ascii_header.c_mode" when calling "from_ascii".
# 1259|   
# 1260|     file_hdr->c_ino = FROM_HEX (ascii_header.c_ino);
# 1261|->   file_hdr->c_mode = FROM_HEX (ascii_header.c_mode);
# 1262|     file_hdr->c_uid = FROM_HEX (ascii_header.c_uid);
# 1263|     file_hdr->c_gid = FROM_HEX (ascii_header.c_gid);', true, 'Non-Issue', 'initialized by the "tape_buffered read"', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.051962');
INSERT INTO public.ground_truth VALUES (91, 'cpio-2.15-1.el10', 17, 'Error: UNINIT (CWE-457):
cpio-2.15/src/copyin.c:1255: var_decl: Declaring variable "ascii_header" without initializer.
cpio-2.15/src/copyin.c:1267: uninit_use_in_call: Using uninitialized element of array "ascii_header.c_dev_maj" when calling "from_ascii".
# 1265|     file_hdr->c_mtime = FROM_HEX (ascii_header.c_mtime);
# 1266|     file_hdr->c_filesize = FROM_HEX (ascii_header.c_filesize);
# 1267|->   file_hdr->c_dev_maj = FROM_HEX (ascii_header.c_dev_maj);
# 1268|     file_hdr->c_dev_min = FROM_HEX (ascii_header.c_dev_min);
# 1269|     file_hdr->c_rdev_maj = FROM_HEX (ascii_header.c_rdev_maj);', true, 'Non-Issue', 'initialized by the "tape_buffered read"', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.051962');
INSERT INTO public.ground_truth VALUES (92, 'cpio-2.15-1.el10', 18, 'Error: UNINIT (CWE-457):
cpio-2.15/src/copyin.c:1255: var_decl: Declaring variable "ascii_header" without initializer.
cpio-2.15/src/copyin.c:1265: uninit_use_in_call: Using uninitialized element of array "ascii_header.c_mtime" when calling "from_ascii".
# 1263|     file_hdr->c_gid = FROM_HEX (ascii_header.c_gid);
# 1264|     file_hdr->c_nlink = FROM_HEX (ascii_header.c_nlink);
# 1265|->   file_hdr->c_mtime = FROM_HEX (ascii_header.c_mtime);
# 1266|     file_hdr->c_filesize = FROM_HEX (ascii_header.c_filesize);
# 1267|     file_hdr->c_dev_maj = FROM_HEX (ascii_header.c_dev_maj);', true, 'Non-Issue', 'initialized by the "tape_buffered read"', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.051962');
INSERT INTO public.ground_truth VALUES (93, 'cpio-2.15-1.el10', 19, 'Error: UNINIT (CWE-457):
cpio-2.15/gnu/argp-help.c:471: alloc_fn: Calling "malloc" which returns uninitialized memory.
cpio-2.15/gnu/argp-help.c:471: assign: Assigning: "hol->short_options" = "malloc(num_short_options + 1U)", which points to uninitialized data.
cpio-2.15/gnu/argp-help.c:496: uninit_use_in_call: Using uninitialized value "*hol->short_options" when calling "find_char".
#  494|               {
#  495|                 entry->num++;
#  496|->               if (oshort (o) && ! find_char (o->key, hol->short_options, so))
#  497|                   /* O has a valid short option which hasn''t already been used.*/
#  498|                   *so++ = o->key;', true, 'Non-Issue', 'the "hol->short_options"  is the start of array and "so" is the end. The "hol->short_options - so" is the number of defined elements of the array and  the find_char() function counts with this situation.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.051962');
INSERT INTO public.ground_truth VALUES (94, 'cpio-2.15-1.el10', 20, 'Error: UNINIT (CWE-457):
cpio-2.15/src/copyin.c:1255: var_decl: Declaring variable "ascii_header" without initializer.
cpio-2.15/src/copyin.c:1271: uninit_use_in_call: Using uninitialized element of array "ascii_header.c_chksum" when calling "from_ascii".
# 1269|     file_hdr->c_rdev_maj = FROM_HEX (ascii_header.c_rdev_maj);
# 1270|     file_hdr->c_rdev_min = FROM_HEX (ascii_header.c_rdev_min);
# 1271|->   file_hdr->c_chksum = FROM_HEX (ascii_header.c_chksum);
# 1272|     read_name_from_file (file_hdr, in_des, FROM_HEX (ascii_header.c_namesize));
# 1273|', true, 'Non-Issue', 'initialized by the "tape_buffered read"', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.051962');
INSERT INTO public.ground_truth VALUES (95, 'cpio-2.15-1.el10', 21, 'Error: BUFFER_SIZE (CWE-170):
cpio-2.15/src/tar.c:176: buffer_size_warning: Calling "strncpy" with a maximum size argument of 100 bytes on destination array "tar_hdr->linkname" of size 100 bytes might leave the destination string unterminated.
#  174|   	  /* process_copy_out makes sure that c_tar_linkname is shorter
#  175|   	     than TARLINKNAMESIZE.  */
#  176|-> 	  strncpy (tar_hdr->linkname, file_hdr->c_tar_linkname,
#  177|   		   TARLINKNAMESIZE);
#  178|   	  tar_hdr->typeflag = LNKTYPE;', false, 'Non-Issue', 'possibly -- easy fix to change TARLINKNAMESIZE to TARLINKNAMESIZE-1 in the strncpy', 'The destination string `tar_hdr->linkname` is guaranteed to be terminated due to `process_copy_out` ensuring `c_tar_linkname` is shorter than `TARLINKNAMESIZE` (line 174), matching the destination array size (100 bytes), thus mitigating the unterminated string risk.', '2025-11-18 16:18:56.051962');
INSERT INTO public.ground_truth VALUES (96, 'cpio-2.15-1.el10', 22, 'Error: OVERRUN (CWE-119):
cpio-2.15/gnu/nstrftime.c:689: assignment: Assigning: "width" = "2147483647".
cpio-2.15/gnu/nstrftime.c:1009: alias: Assigning: "bufp" = "buf + 23UL". "bufp" now points to byte 23 of "buf" (which consists of 23 bytes).
cpio-2.15/gnu/nstrftime.c:1019: ptr_decr: Decrementing "bufp". "bufp" now points to byte 22 of "buf" (which consists of 23 bytes).
cpio-2.15/gnu/nstrftime.c:1048: decr: Decrementing "width". The value of "width" is now 2147483646.
cpio-2.15/gnu/nstrftime.c:1051: assignment: Assigning: "_w" = "(pad == 45 || width < 0) ? 0 : width". The value of "_w" is now 2147483646.
cpio-2.15/gnu/nstrftime.c:1051: cond_at_most: Checking "_n < _w" implies that "_n" may be up to 2147483645 on the true branch.
cpio-2.15/gnu/nstrftime.c:1051: overrun-buffer-arg: Overrunning buffer pointed to by "(void const *)bufp" of 23 bytes by passing it to a function which accesses it at byte offset 2147483666 using argument "_n" (which evaluates to 2147483645). [Note: The source code implementation of the function has been overridden by a builtin model.]
# 1049|                 }
# 1050|   
# 1051|->             cpy (numlen, bufp);
# 1052|             }
# 1053|             break;', false, 'Non-Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-44929

not sure, but probably misplaced "{" bracket on line 1030. I think it should be on line 1027', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.051962');
INSERT INTO public.ground_truth VALUES (97, 'cpio-2.15-1.el10', 23, 'Error: OVERRUN (CWE-119):
cpio-2.15/gnu/nstrftime.c:689: assignment: Assigning: "width" = "2147483647".
cpio-2.15/gnu/nstrftime.c:885: assignment: Assigning: "_w" = "(pad == 45 || width < 0) ? 0 : width". The value of "_w" is now 2147483647.
cpio-2.15/gnu/nstrftime.c:885: cond_between: Checking "_n < _w" implies that "_n" is between 0 and 2147483646 (inclusive) on the true branch.
cpio-2.15/gnu/nstrftime.c:885: overrun-buffer-arg: Overrunning buffer pointed to by "(void const *)(ubuf + 1)" of 1024 bytes by passing it to a function which accesses it at byte offset 2147483646 using argument "_n" (which evaluates to 2147483646). [Note: The source code implementation of the function has been overridden by a builtin model.]
#  883|               len = strftime (ubuf, sizeof ubuf, ufmt, tp);
#  884|               if (len != 0)
#  885|->               cpy (len - 1, ubuf + 1);
#  886|             }
#  887|             break;', true, 'Non-Issue', NULL, 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.051962');
INSERT INTO public.ground_truth VALUES (98, 'cpio-2.15-1.el10', 24, 'Error: UNINIT (CWE-457):
cpio-2.15/src/copyin.c:1196: var_decl: Declaring variable "ascii_header" without initializer.
cpio-2.15/src/copyin.c:1207: uninit_use_in_call: Using uninitialized element of array "ascii_header.c_uid" when calling "from_ascii".
# 1205|     file_hdr->c_ino = FROM_OCTAL (ascii_header.c_ino);
# 1206|     file_hdr->c_mode = FROM_OCTAL (ascii_header.c_mode);
# 1207|->   file_hdr->c_uid = FROM_OCTAL (ascii_header.c_uid);
# 1208|     file_hdr->c_gid = FROM_OCTAL (ascii_header.c_gid);
# 1209|     file_hdr->c_nlink = FROM_OCTAL (ascii_header.c_nlink);', true, 'Non-Issue', 'initialized by the "tape_buffered read"', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.051962');
INSERT INTO public.ground_truth VALUES (100, 'cpio-2.15-1.el10', 26, 'Error: UNINIT (CWE-457):
cpio-2.15/src/copyin.c:1196: var_decl: Declaring variable "ascii_header" without initializer.
cpio-2.15/src/copyin.c:1216: uninit_use_in_call: Using uninitialized element of array "ascii_header.c_namesize" when calling "from_ascii".
# 1214|     file_hdr->c_mtime = FROM_OCTAL (ascii_header.c_mtime);
# 1215|     file_hdr->c_filesize = FROM_OCTAL (ascii_header.c_filesize);
# 1216|->   read_name_from_file (file_hdr, in_des, FROM_OCTAL (ascii_header.c_namesize));
# 1217|   
# 1218|     /* HP/UX cpio creates archives that look just like ordinary archives,', true, 'Non-Issue', 'initialized by the "tape_buffered read"', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.051962');
INSERT INTO public.ground_truth VALUES (101, 'cpio-2.15-1.el10', 27, 'Error: UNINIT (CWE-457):
cpio-2.15/src/copyin.c:1255: var_decl: Declaring variable "ascii_header" without initializer.
cpio-2.15/src/copyin.c:1266: uninit_use_in_call: Using uninitialized element of array "ascii_header.c_filesize" when calling "from_ascii".
# 1264|     file_hdr->c_nlink = FROM_HEX (ascii_header.c_nlink);
# 1265|     file_hdr->c_mtime = FROM_HEX (ascii_header.c_mtime);
# 1266|->   file_hdr->c_filesize = FROM_HEX (ascii_header.c_filesize);
# 1267|     file_hdr->c_dev_maj = FROM_HEX (ascii_header.c_dev_maj);
# 1268|     file_hdr->c_dev_min = FROM_HEX (ascii_header.c_dev_min);', true, 'Non-Issue', 'initialized by the "tape_buffered read"', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.051962');
INSERT INTO public.ground_truth VALUES (102, 'cpio-2.15-1.el10', 28, 'Error: INTEGER_OVERFLOW (CWE-190):
cpio-2.15/lib/rtapelib.c:604: tainted_data_return: Called function "get_status(handle)", and a possible return value may be less than zero.
cpio-2.15/lib/rtapelib.c:604: cast_underflow: An assign of a possibly negative number to an unsigned type, which might trigger an underflow.
cpio-2.15/lib/rtapelib.c:611: overflow: The expression "status - counter" is deemed underflowed because at least one of its arguments has underflowed.
cpio-2.15/lib/rtapelib.c:611: overflow_sink: "status - counter", which might have underflowed, is passed to "safe_read(from_remote[handle][0], buffer, status - counter)".
#  609|     for (counter = 0; counter < status; counter += rlen, buffer += rlen)
#  610|       {
#  611|->       rlen = safe_read (READ_SIDE (handle), buffer, status - counter);
#  612|         if (rlen == SAFE_READ_ERROR || rlen == 0)
#  613|   	{', true, 'Non-Issue', 'get_status may return values from range <-1; LONG_INT_MAX>. If it returns -1 (that is equal to SAFE_READ_ERROR), then the rmr_read__ function returns on line 607', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.051962');
INSERT INTO public.ground_truth VALUES (103, 'cpio-2.15-1.el10', 29, 'Error: UNINIT (CWE-457):
cpio-2.15/src/copyin.c:1255: var_decl: Declaring variable "ascii_header" without initializer.
cpio-2.15/src/copyin.c:1262: uninit_use_in_call: Using uninitialized element of array "ascii_header.c_uid" when calling "from_ascii".
# 1260|     file_hdr->c_ino = FROM_HEX (ascii_header.c_ino);
# 1261|     file_hdr->c_mode = FROM_HEX (ascii_header.c_mode);
# 1262|->   file_hdr->c_uid = FROM_HEX (ascii_header.c_uid);
# 1263|     file_hdr->c_gid = FROM_HEX (ascii_header.c_gid);
# 1264|     file_hdr->c_nlink = FROM_HEX (ascii_header.c_nlink);', true, 'Non-Issue', 'initialized by the "tape_buffered read"', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.051962');
INSERT INTO public.ground_truth VALUES (105, 'cpio-2.15-1.el10', 31, 'Error: UNINIT (CWE-457):
cpio-2.15/gnu/time_rz.c:294: var_decl: Declaring variable "tm_1" without initializer.
cpio-2.15/gnu/time_rz.c:306: uninit_use_in_call: Using uninitialized value "tm_1.tm_zone" when calling "save_abbr".
#  304|             bool ok = 0 <= tm_1.tm_yday;
#  305|   #if HAVE_STRUCT_TM_TM_ZONE || HAVE_TZNAME
#  306|->           ok = ok && save_abbr (tz, &tm_1);
#  307|   #endif
#  308|             if (revert_tz (old_tz) && ok)', true, 'Non-Issue', NULL, 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.051962');
INSERT INTO public.ground_truth VALUES (106, 'cpio-2.15-1.el10', 32, 'Error: UNINIT (CWE-457):
cpio-2.15/src/copyin.c:1196: var_decl: Declaring variable "ascii_header" without initializer.
cpio-2.15/src/copyin.c:1208: uninit_use_in_call: Using uninitialized element of array "ascii_header.c_gid" when calling "from_ascii".
# 1206|     file_hdr->c_mode = FROM_OCTAL (ascii_header.c_mode);
# 1207|     file_hdr->c_uid = FROM_OCTAL (ascii_header.c_uid);
# 1208|->   file_hdr->c_gid = FROM_OCTAL (ascii_header.c_gid);
# 1209|     file_hdr->c_nlink = FROM_OCTAL (ascii_header.c_nlink);
# 1210|     dev = FROM_OCTAL (ascii_header.c_rdev);', true, 'Non-Issue', 'initialized by the "tape_buffered read"', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.051962');
INSERT INTO public.ground_truth VALUES (107, 'cpio-2.15-1.el10', 33, 'Error: UNINIT (CWE-457):
cpio-2.15/src/copyin.c:1255: var_decl: Declaring variable "ascii_header" without initializer.
cpio-2.15/src/copyin.c:1264: uninit_use_in_call: Using uninitialized element of array "ascii_header.c_nlink" when calling "from_ascii".
# 1262|     file_hdr->c_uid = FROM_HEX (ascii_header.c_uid);
# 1263|     file_hdr->c_gid = FROM_HEX (ascii_header.c_gid);
# 1264|->   file_hdr->c_nlink = FROM_HEX (ascii_header.c_nlink);
# 1265|     file_hdr->c_mtime = FROM_HEX (ascii_header.c_mtime);
# 1266|     file_hdr->c_filesize = FROM_HEX (ascii_header.c_filesize);', true, 'Non-Issue', 'initialized by the "tape_buffered read"', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.051962');
INSERT INTO public.ground_truth VALUES (108, 'glibc-2.39-2.el10', 1, 'Error: OVERRUN (CWE-119):
glibc-2.39/libio/iofdopen.c:157: overrun-local: Overrunning array of 27 8-byte elements at element index 27 (byte offset 223) by dereferencing pointer "(struct _IO_jump_t const **)((char *)&new_f->fp.file + 216UL)".
#  155|                     == (_IO_IS_APPENDING | _IO_NO_READS)))
#  156|       {
#  157|->       off64_t new_pos = _IO_SYSSEEK (&new_f->fp.file, 0, _IO_seek_end);
#  158|         if (new_pos == _IO_pos_BAD && errno != ESPIPE)
#  159|           return NULL;', true, 'Non-Issue', 'Checker is confusing the public definition of FILE with glibc''s internal (and larger) _IO_FILE_plus, which has an extra field in it.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (109, 'glibc-2.39-2.el10', 2, 'Error: UNINIT (CWE-457):
glibc-2.39/locale/programs/ld-collate.c:2110: var_decl: Declaring variable "extrapool" without initializer.
glibc-2.39/locale/programs/ld-collate.c:2250: assign: Assigning: "__o" = "&extrapool", which points to uninitialized data.
glibc-2.39/locale/programs/ld-collate.c:2250: uninit_use_in_call: Using uninitialized value "__o->extra_arg" when calling "_obstack_newchunk".
# 2248|   					  + 2 * (runp->nmbs - 1));
# 2249|   		assert (LOCFILE_ALIGNED_P (obstack_object_size (&extrapool)));
# 2250|-> 		obstack_make_room (&extrapool, added);
# 2251|   
# 2252|   		/* More than one consecutive entry.  We mark this by having', true, 'Non-Issue', 'obstack_init() initialized it. extra_arg is only used if use_extra_arg is set, which only happens when extra_arg is set.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (110, 'glibc-2.39-2.el10', 3, 'Error: OVERRUN (CWE-119):
glibc-2.39/time/tzset.c:395: alias: Assigning: "tz" = ""/etc/localtime"". "tz" now points to byte 0 of ""/etc/localtime"" (which consists of 15 bytes).
glibc-2.39/time/tzset.c:405: overrun-buffer-val: Overrunning buffer pointed to by "tz" of 15 bytes by passing it to a function which accesses it at byte offset 18.
#  403|   
#  404|     /* Try to read a data file.  */
#  405|->   __tzfile_read (tz, 0, NULL);
#  406|     if (__use_tzfile)
#  407|       return;', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-45144

', 'Assignment at `tzset.c:395` and subsequent call to `__tzfile_read` at `tzset.c:405` with no evident bounds checking, potentially leading to a buffer overrun since `tz` points to a 15-byte buffer (`/etc/localtime`) that may be accessed at offset 18.', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (111, 'glibc-2.39-2.el10', 4, 'Error: UNINIT (CWE-457):
glibc-2.39/libio/obprintf.c:82: var_decl: Declaring variable "buf" without initializer.
glibc-2.39/libio/obprintf.c:99: uninit_use: Using uninitialized value "buf.ch".
#   97|     if (buf.base.write_ptr == &buf.ch + 1)
#   98|       /* buf.ch is in use.  Put it into the obstack.  */
#   99|->     obstack_1grow (buf.obstack, buf.ch);
#  100|     else if (buf.base.write_ptr != &buf.ch)
#  101|       /* Shrink the buffer to the space we really currently need.  */', true, 'Non-Issue', 'ch is used as a temporary buffer, set up by __printf_buffer_flush_obstack, and protected by write_base and write_ptr.  It will only be read if it''s been set up as a queue and the queue written to.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (112, 'glibc-2.39-2.el10', 5, 'Error: INTEGER_OVERFLOW (CWE-125):
glibc-2.39/debug/pcprofiledump.c:136: tainted_data_return: Called function "read(fd, &pair.bytes[8UL - len], len)", and a possible return value may be less than zero.
glibc-2.39/debug/pcprofiledump.c:136: cast_overflow: An assign that casts to a different type, which might trigger an overflow.
glibc-2.39/debug/pcprofiledump.c:135: cast_underflow: An assign of a possibly negative number to an unsigned type, which might trigger an underflow.
glibc-2.39/debug/pcprofiledump.c:138: overflow: The expression "len -= n" is deemed underflowed because at least one of its arguments has underflowed.
glibc-2.39/debug/pcprofiledump.c:136: overflow: The expression "8UL - len" is deemed underflowed because at least one of its arguments has underflowed.
glibc-2.39/debug/pcprofiledump.c:136: deref_overflow: "8UL - len", which might have underflowed, is passed to "pair.bytes[8UL - len]".
#  134|   
#  135|   	  while (len > 0
#  136|-> 		 && (n = TEMP_FAILURE_RETRY (read (fd, &pair.bytes[8 - len],
#  137|   						   len))) != 0)
#  138|   	    len -= n;', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-45143

', 'Unchecked negative return value from `read` allows potential underflow in `len -= n` (lines 136-138), directly correlating with CVE descriptions of `tainted_data_return`, `cast_underflow`, and `overflow`, with no explicit safeguard present.', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (127, 'glibc-2.39-2.el10', 20, 'Error: UNINIT (CWE-457):
glibc-2.39/nss/getaddrinfo.c:2034: var_decl: Declaring variable "endp" without initializer.
glibc-2.39/nss/getaddrinfo.c:2044: uninit_use: Using uninitialized value "endp".
# 2042|   		{
# 2043|   		  bits = 128;
# 2044|-> 		  if (IN6_IS_ADDR_V4MAPPED (&prefix)
# 2045|   		      && (cp == NULL
# 2046|   			  || (bits = strtoul (cp, &endp, 10)) != ULONG_MAX', false, 'Non-Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-45143

', 'Variable ''endp'' is either initialized by ''strtoul'' on line 2046 when ''cp'' is not NULL, or its potential uninitialized use is avoided due to short-circuiting in the conditional statement when ''cp'' is NULL (lines 2045-2046), mitigating the reported vulnerability.', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (340, 'sqlite-3.45.1-2.el10', 9, 'Error: USE_AFTER_FREE (CWE-416):
sqlite-src-3450100/sqlite3.c:66006: freed_arg: "walIndexAppend" frees "pWal->apWiData".
sqlite-src-3450100/sqlite3.c:66020: use_after_free: Using freed pointer "pWal->apWiData".
#66018|           }
#66019|         }
#66020|->       pWal->apWiData[iPg] = aShare;
#66021|         SEH_SET_ON_ERROR(0,0);
#66022|         nHdr = (iPg==0 ? WALINDEX_HDR_SIZE : 0);', true, 'Non-Issue', ' wallIndexAppend does not free pWal->apWiData, but reallocates it.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (113, 'glibc-2.39-2.el10', 6, 'Error: INTEGER_OVERFLOW (CWE-125):
glibc-2.39/debug/pcprofiledump.c:163: tainted_data_return: Called function "read(fd, &pair.bytes[8UL - len], len)", and a possible return value may be less than zero.
glibc-2.39/debug/pcprofiledump.c:163: cast_overflow: An assign that casts to a different type, which might trigger an overflow.
glibc-2.39/debug/pcprofiledump.c:162: cast_underflow: An assign of a possibly negative number to an unsigned type, which might trigger an underflow.
glibc-2.39/debug/pcprofiledump.c:165: overflow: The expression "len -= n" is deemed underflowed because at least one of its arguments has underflowed.
glibc-2.39/debug/pcprofiledump.c:163: overflow: The expression "8UL - len" is deemed underflowed because at least one of its arguments has underflowed.
glibc-2.39/debug/pcprofiledump.c:163: deref_overflow: "8UL - len", which might have underflowed, is passed to "pair.bytes[8UL - len]".
#  161|   
#  162|   	  while (len > 0
#  163|-> 		 && (n = TEMP_FAILURE_RETRY (read (fd, &pair.bytes[8 - len],
#  164|   						   len))) != 0)
#  165|   	    len -= n;', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-45143

', 'Unchecked negative return value from `read` (line 163) can lead to underflow in `len -= n` (line 165) since `len` is an unsigned `size_t`, and there''s no explicit error handling to prevent this scenario, aligning with reported overflow and underflow concerns.', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (114, 'glibc-2.39-2.el10', 7, 'Error: INTEGER_OVERFLOW (CWE-190):
glibc-2.39/iconv/iconv_charmap.c:496: tainted_data_argument: The check "actlen < maxlen" contains the tainted expression "actlen" which causes "maxlen" to be considered tainted.
glibc-2.39/iconv/iconv_charmap.c:498: overflow: The expression "maxlen - actlen" is deemed underflowed because at least one of its arguments has underflowed.
glibc-2.39/iconv/iconv_charmap.c:498: overflow_sink: "maxlen - actlen", which might have underflowed, is passed to "read(fd, inptr, maxlen - actlen)". [Note: The source code implementation of the function has been overridden by a builtin model.]
#  496|     while (actlen < maxlen)
#  497|       {
#  498|->       ssize_t n = read (fd, inptr, maxlen - actlen);
#  499|   
#  500|         if (n == 0)', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-45143

', 'The expression `maxlen - actlen` (line 498) can underflow if `actlen` exceeds `maxlen`, and this potentially underflowed value is passed to `read()`, aligning with CWE-190, without explicit bounds checking or inherent mitigation in the provided code.', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (115, 'glibc-2.39-2.el10', 8, 'Error: INTEGER_OVERFLOW (CWE-190):
glibc-2.39/iconv/iconv_charmap.c:498: tainted_data_return: Called function "read(fd, inptr, maxlen - actlen)", and a possible return value may be less than zero.
glibc-2.39/iconv/iconv_charmap.c:498: assign: Assigning: "n" = "read(fd, inptr, maxlen - actlen)".
glibc-2.39/iconv/iconv_charmap.c:512: overflow: The expression "actlen += n" might be negative, but is used in a context that treats it as unsigned.
glibc-2.39/iconv/iconv_charmap.c:558: overflow_sink: "actlen", which might be negative, is passed to "process_block(tbl, inbuf, actlen, output)".
#  556|   
#  557|     /* Now we have all the input in the buffer.  Process it in one run.  */
#  558|->   return process_block (tbl, inbuf, actlen, output);
#  559|   }
#  560|', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-45143

', 'Integer overflow vulnerability (CWE-190) is possible due to the addition of a potentially negative `n` (from `read()` at iconv_charmap.c:498) to unsigned `actlen` at iconv_charmap.c:512, which is then passed to `process_block()` at iconv_charmap.c:558 without underflow correction, leading to potential security issues.', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (116, 'glibc-2.39-2.el10', 9, 'Error: INTEGER_OVERFLOW (CWE-190):
glibc-2.39/iconv/iconv_prog.c:550: tainted_data_argument: The check "actlen < maxlen" contains the tainted expression "actlen" which causes "maxlen" to be considered tainted.
glibc-2.39/iconv/iconv_prog.c:552: overflow: The expression "maxlen - actlen" is deemed underflowed because at least one of its arguments has underflowed.
glibc-2.39/iconv/iconv_prog.c:552: overflow_sink: "maxlen - actlen", which might have underflowed, is passed to "read(fd, inptr, maxlen - actlen)". [Note: The source code implementation of the function has been overridden by a builtin model.]
#  550|     while (actlen < maxlen)
#  551|       {
#  552|->       ssize_t n = read (fd, inptr, maxlen - actlen);
#  553|   
#  554|         if (n == 0)', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-45143

', 'Potential underflow in `maxlen - actlen` (line 552) due to tainted `actlen`, with no explicit bounds checking or validation to prevent manipulation, creating a possible vulnerable execution path for CWE-190: Integer Overflow or Wraparound.', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (117, 'glibc-2.39-2.el10', 10, 'Error: INTEGER_OVERFLOW (CWE-190):
glibc-2.39/iconv/iconv_prog.c:552: tainted_data_return: Called function "read(fd, inptr, maxlen - actlen)", and a possible return value may be less than zero.
glibc-2.39/iconv/iconv_prog.c:552: assign: Assigning: "n" = "read(fd, inptr, maxlen - actlen)".
glibc-2.39/iconv/iconv_prog.c:566: overflow: The expression "actlen += n" might be negative, but is used in a context that treats it as unsigned.
glibc-2.39/iconv/iconv_prog.c:612: overflow_sink: "actlen", which might be negative, is passed to "process_block(cd, inbuf, actlen, output, output_file)".
#  610|   
#  611|     /* Now we have all the input in the buffer.  Process it in one run.  */
#  612|->   return process_block (cd, inbuf, actlen, output, output_file);
#  613|   }
#  614|', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-45143

', 'Potential negative return value from `read()` at iconv_prog.c:552 can propagate through assignments (`n` at 552, `actlen` at 566) and be passed to `process_block()` at 612 without explicit prevention or handling for negative values, leveraging unsigned context expectations.', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (118, 'glibc-2.39-2.el10', 11, 'Error: OVERRUN (CWE-119):
glibc-2.39/build-x86_64-redhat-linux/intl/plural.c:1044: assignment: Assigning: "yystacksize" = "200L".
glibc-2.39/build-x86_64-redhat-linux/intl/plural.c:1131: assignment: Assigning: "yystacksize" *= "2L". The value of "yystacksize" is now 400.
glibc-2.39/build-x86_64-redhat-linux/intl/plural.c:1142: alias: Assigning: "yyss" = "&yyptr->yyss_alloc". "yyss" now points to byte 0 of "yyptr->yyss_alloc" (which consists of 8 bytes).
glibc-2.39/build-x86_64-redhat-linux/intl/plural.c:1158: illegal_address: "yyss + yystacksize - 1" evaluates to an address that is at byte offset 399 of an array of 8 bytes.
# 1156|         YY_IGNORE_USELESS_CAST_END
# 1157|   
# 1158|->       if (yyss + yystacksize - 1 <= yyssp)
# 1159|           YYABORT;
# 1160|       }', true, 'Non-Issue', 'The stack is resized with YYSTACK_ALLOC', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (159, 'glibc-2.39-2.el10', 52, 'Error: OVERRUN (CWE-119):
glibc-2.39/nss/nss_compat/compat-spwd.c:536: strlen_assign: Setting variable "len" to the return value of strlen called with argument "result->sp_namp".
glibc-2.39/nss/nss_compat/compat-spwd.c:537: alloc_strlen: Allocating insufficient memory for the terminating null of the string.
#  535|   	{
#  536|   	  size_t len = strlen (result->sp_namp);
#  537|-> 	  char buf[len];
#  538|   	  enum nss_status status;
#  539|', true, 'Non-Issue', 'the value copied in starts at the second character of the string, not the first, leaving room for the NUL', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (119, 'glibc-2.39-2.el10', 12, 'Error: OVERRUN (CWE-119):
glibc-2.39/libio/vasprintf.c:53: cond_at_most: Checking "current_pos >= 2147483647UL" implies that "current_pos" may be up to 2147483646 on the false branch.
glibc-2.39/libio/vasprintf.c:75: overrun-buffer-arg: Overrunning array "buf->direct" of 200 bytes by passing it to a function which accesses it at byte offset 2147483645 using argument "current_pos" (which evaluates to 2147483646). [Note: The source code implementation of the function has been overridden by a builtin model.]
#   73|   	  return;
#   74|   	}
#   75|->       memcpy (new_buffer, buf->direct, current_pos);
#   76|       }
#   77|     else', true, 'Non-Issue', 'The check >= INT_MAX is intended to test for 64-bit pointer math resulting in a size_t that exceeds sizeof(int).  I.e. it''s for systems where the return value of asprintf() would be too large.  It is not related to negative numbers.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (120, 'glibc-2.39-2.el10', 13, 'Error: BUFFER_SIZE (CWE-474):
glibc-2.39/locale/programs/md5.c:173: overlapping_buffer: The source buffer "&(*ctx).buffer[64]" potentially overlaps with the destination buffer "(*ctx).buffer", which results in undefined behavior for "memcpy".
glibc-2.39/locale/programs/md5.c:173: remediation: Use memmove instead of "memcpy".
#  171|   	  __md5_process_block (ctx->buffer, 64, ctx);
#  172|   	  left_over -= 64;
#  173|-> 	  memcpy (ctx->buffer, &ctx->buffer[64], left_over);
#  174|   	}
#  175|         ctx->buflen = left_over;', false, 'Non-Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-45144

', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (121, 'glibc-2.39-2.el10', 14, 'Error: COPY_PASTE_ERROR (CWE-398):
glibc-2.39/scripts/glibcpp.py:507: original: "left.line" looks like the original copy.
glibc-2.39/scripts/glibcpp.py:512: copy_paste_error: "left" in "left.line" looks like a copy-paste error.
glibc-2.39/scripts/glibcpp.py:512: remediation: Should it say "right" instead?
#  510|                                     ''in definition of macro {}''.format(md.name))
#  511|                   if type(right) != type(1):
#  512|->                     reporter.error(left.line,
#  513|                           ''right operand of {} is not an integer''.format(op))
#  514|                       reporter.note(md.line,', true, 'Non-Issue', 'it''s reporting the line number of the start of the expression, not the specific term in it.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (122, 'glibc-2.39-2.el10', 15, 'Error: INTEGER_OVERFLOW (CWE-190):
glibc-2.39/elf/sprof.c:559: tainted_data_argument: The value "*shdr" is considered tainted.
glibc-2.39/elf/sprof.c:564: tainted_data_argument: "shdr[ehdr->e_shstrndx].sh_offset" is considered tainted.
glibc-2.39/elf/sprof.c:564: underflow: The cast of "shdr[ehdr->e_shstrndx].sh_offset" to a signed type could result in a negative number.
#  562|     /* Get the section header string table.  */
#  563|     char *shstrtab = (char *) alloca (shdr[ehdr->e_shstrndx].sh_size);
#  564|->   if (pread (fd, shstrtab, shdr[ehdr->e_shstrndx].sh_size,
#  565|   	     shdr[ehdr->e_shstrndx].sh_offset)
#  566|         != shdr[ehdr->e_shstrndx].sh_size)', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-45143

', 'Tainted ELF data (`*shdr` and `shdr[ehdr->e_shstrndx].sh_offset`) is used without explicit validation or sanitization, potentially leading to underflow issues when cast to a signed type (line 564), with no clear safeguards in the provided code snippet.', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (123, 'glibc-2.39-2.el10', 16, 'Error: INTEGER_OVERFLOW (CWE-190):
glibc-2.39/elf/sprof.c:653: tainted_data_argument: The value "*shdr2" is considered tainted.
glibc-2.39/elf/sprof.c:659: tainted_data_argument: "shdr2[ehdr2.e_shstrndx].sh_offset" is considered tainted.
glibc-2.39/elf/sprof.c:659: underflow: The cast of "shdr2[ehdr2.e_shstrndx].sh_offset" to a signed type could result in a negative number.
#  657|   	  /* Get the section header string table.  */
#  658|   	  shstrtab = (char *) alloca (shdr2[ehdr2.e_shstrndx].sh_size);
#  659|-> 	  if (pread (fd2, shstrtab, shdr2[ehdr2.e_shstrndx].sh_size,
#  660|   		     shdr2[ehdr2.e_shstrndx].sh_offset)
#  661|   	      != shdr2[ehdr2.e_shstrndx].sh_size)', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-45143

', 'Tainted ELF file data (`*shdr2` and `shdr2[ehdr2.e_shstrndx].sh_offset`) is used without validation in memory allocation (`alloca`, line 658) and file reading (`pread`, line 659), potentially leading to underflow and unexpected behavior, with no explicit input sanitization or proof of safety in the provided code.', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (124, 'glibc-2.39-2.el10', 17, 'Error: UNEXPECTED_CONTROL_FLOW (CWE-398):
glibc-2.39/iconv/loop.c:374: continue_in_do_while_false: A "continue" statement within a "do ... while (...)" loop only continues execution of the loop body if the loop continuation condition is still true.  Since the condition will never be true in a "do ... while (false)" loop, the "continue"statement has the same effect as a "break" statement.  Did you intend execution to continue at the top of the loop?
glibc-2.39/iconv/loop.c:376: do_while_false_condition: This loop will never continue since the condition "0" is never true.
#  372|     do
#  373|       {
#  374|->       BODY
#  375|       }
#  376|     while (0);', false, 'Non-Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-45143

', 'The reported `continue` statement issue is a FALSE POSITIVE since the provided code (lines 372-376) lacks a `continue` statement within the `do ... while (false)` loop, rendering the warning irrelevant to potential security vulnerabilities.', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (125, 'glibc-2.39-2.el10', 18, 'Error: UNINIT (CWE-457):
glibc-2.39/elf/dl-load.c:2015: skipped_decl: Jumping over declaration of "found_other_class".
glibc-2.39/elf/dl-load.c:2238: uninit_use: Using uninitialized value "found_other_class".
# 2236|   	  return l;
# 2237|   	}
# 2238|->       else if (found_other_class)
# 2239|   	_dl_signal_error (0, name, NULL,
# 2240|   			  ELFW(CLASS) == ELFCLASS32', false, 'Non-Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-45144

', 'Variable `found_other_class` is explicitly declared and initialized as `false` on line 2015, within the same scope as its usage on line 2238, with no intervening unconditional modifications, ensuring it is not uninitialized when used.', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (126, 'glibc-2.39-2.el10', 19, 'Error: UNINIT (CWE-457):
glibc-2.39/nss/getaddrinfo.c:1875: var_decl: Declaring variable "endp" without initializer.
glibc-2.39/nss/getaddrinfo.c:1883: uninit_use: Using uninitialized value "endp".
# 1881|       *cp++ = ''\0'';
# 1882|     *pos = cp;
# 1883|->   if (inet_pton (AF_INET6, val1, &prefix)
# 1884|         && (cp == NULL
# 1885|   	  || (bits = strtoul (cp, &endp, 10)) != ULONG_MAX', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-45143

', 'Variable `endp` is declared without an initializer (line 1875) and, in the execution path where `cp == NULL`, its use in the conditional statement (line 1885) occurs before potential initialization by `strtoul()`, due to short-circuit evaluation, thus constituting a vulnerability.', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (128, 'glibc-2.39-2.el10', 21, 'Error: UNINIT (CWE-457):
glibc-2.39/posix/regcomp.c:1173: alloc_fn: Calling "malloc" which returns uninitialized memory. [Note: The source code implementation of the function has been overridden by a builtin model.]
glibc-2.39/posix/regcomp.c:1173: assign: Assigning: "dfa->eclosures" = "(re_node_set *)malloc(dfa->nodes_alloc * 16UL)", which points to uninitialized data.
glibc-2.39/posix/regcomp.c:1205: uninit_use_in_call: Using uninitialized value "dfa->eclosures->nelem" when calling "calc_eclosure".
# 1203|     if (__glibc_unlikely (ret != REG_NOERROR))
# 1204|       return ret;
# 1205|->   ret = calc_eclosure (dfa);
# 1206|     if (__glibc_unlikely (ret != REG_NOERROR))
# 1207|       return ret;', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-45143

', 'Uninitialized memory allocated for `dfa->eclosures` at line 1173 is used without explicit initialization at line 1205 in `calc_eclosure(dfa)`, directly correlating with the CVE''s described vulnerability of using uninitialized memory.', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (129, 'glibc-2.39-2.el10', 22, 'Error: UNINIT (CWE-457):
glibc-2.39/posix/regcomp.c:1173: alloc_fn: Calling "malloc" which returns uninitialized memory. [Note: The source code implementation of the function has been overridden by a builtin model.]
glibc-2.39/posix/regcomp.c:1173: assign: Assigning: "dfa->eclosures" = "(re_node_set *)malloc(dfa->nodes_alloc * 16UL)", which points to uninitialized data.
glibc-2.39/posix/regcomp.c:1217: uninit_use_in_call: Using uninitialized value "dfa->eclosures->elems" when calling "calc_inveclosure".
# 1215|         if (__glibc_unlikely (dfa->inveclosures == NULL))
# 1216|   	return REG_ESPACE;
# 1217|->       ret = calc_inveclosure (dfa);
# 1218|       }
# 1219|', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-45143

', 'Uninitialized memory allocated for `dfa->eclosures` at `regcomp.c:1173` is directly used via `dfa->eclosures->elems` in `calc_inveclosure` at `regcomp.c:1217` without explicit initialization, correlating with the CVE''s description of using uninitialized data.', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (130, 'glibc-2.39-2.el10', 23, 'Error: UNINIT (CWE-457):
glibc-2.39/sysdeps/pthread/sem_open.c:41: var_decl: Declaring variable "result" without initializer.
glibc-2.39/sysdeps/pthread/sem_open.c:203: uninit_use: Using uninitialized value "result".
#  201|   
#  202|     /* Map the mmap error to the error we need.  */
#  203|->   if (MAP_FAILED != (void *) SEM_FAILED && result == MAP_FAILED)
#  204|       result = SEM_FAILED;
#  205|', false, 'Non-Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-45143

', 'Multiple execution paths initialize ''result'' before its use at line 203, and the self-protective check at line 203 (`result == MAP_FAILED`) mitigates the risk of exploiting an uninitialized variable, making the reported vulnerability conditional and not unequivocally exploitable.', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (131, 'glibc-2.39-2.el10', 24, 'Error: USE_AFTER_FREE (CWE-416):
glibc-2.39/support/resolv_test.c:329: alias: Equality between "crname_target" and "crname" implies that they are aliases.
glibc-2.39/support/resolv_test.c:331: freed_arg: "free" frees "crname". [Note: The source code implementation of the function has been overridden by a builtin model.]
glibc-2.39/support/resolv_test.c:338: deref_after_free: Dereferencing freed pointer "crname_target".
#  336|           {
#  337|             /* The name is known.  Reference the previous location.  */
#  338|->           unsigned int old_offset = crname_target->offset;
#  339|             TEST_VERIFY_EXIT (old_offset < compression_limit);
#  340|             response_add_byte (b, 0xC0 | (old_offset >> 8));', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-45145

', 'Dereferencing `crname_target->offset` at line 338 occurs after `crname` (its alias) is explicitly freed at line 331, with no intervening reallocation, directly matching the Use After Free (CWE-416) vulnerability described in the CVE report.', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (132, 'glibc-2.39-2.el10', 25, 'Error: INTEGER_OVERFLOW (CWE-190):
glibc-2.39/elf/sprof.c:646: tainted_data_argument: The value "ehdr2" is considered tainted.
glibc-2.39/elf/sprof.c:653: tainted_data_argument: "ehdr2.e_shoff" is considered tainted.
glibc-2.39/elf/sprof.c:653: underflow: The cast of "ehdr2.e_shoff" to a signed type could result in a negative number.
#  651|   	  size_t size = ehdr2.e_shnum * sizeof (ElfW(Shdr));
#  652|   	  ElfW(Shdr) *shdr2 = (ElfW(Shdr) *) alloca (size);
#  653|-> 	  if (pread (fd2, shdr2, size, ehdr2.e_shoff) != size)
#  654|   	    error (EXIT_FAILURE, errno,
#  655|   		   _("reading of section headers failed"));', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-45143

', 'Unvalidated, potentially tainted `ehdr2.e_shoff` is cast to a signed type and used as an offset in `pread` (line 653), risking underflow and unexpected behavior, with no visible mitigating checks in the provided code.', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (133, 'glibc-2.39-2.el10', 26, 'Error: RESOURCE_LEAK (CWE-772):
glibc-2.39/support/test-container.c:700: alloc_fn: Storage is returned from allocation function "fopen".
glibc-2.39/support/test-container.c:700: var_assign: Assigning: "f" = storage returned from "fopen(files[i].path, "r")".
glibc-2.39/support/test-container.c:705: noescape: Resource "f" is not freed or pointed-to in "fscanf".
glibc-2.39/support/test-container.c:700: overwrite_var: Overwriting "f" in "f = fopen(files[i].path, "r")" leaks the storage that "f" points to.
#  698|           continue;
#  699|   
#  700|->       f = fopen (files[i].path, "r");
#  701|         if (f == NULL)
#  702|           continue;', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-45145

', 'Resource allocated by `fopen` at line 700 is not freed, leading to a potential resource leak (CWE-772), as evidenced by the lack of `fclose` within the provided code snippet, exacerbated by overwriting `f` without release on subsequent iterations.', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (134, 'glibc-2.39-2.el10', 27, 'Error: CPPCHECK_WARNING (CWE-686):
glibc-2.39/timezone/zic.c:2270: error[invalidFunctionArgBool]: Invalid putc() argument nr 1. A non-boolean value is required.
# 2268|                     for (i = old0; i < typecnt; i++)
# 2269|                           if (!omittype[i])
# 2270|->                                 putc(ttisstds[i], fp);
# 2271|                   if (utcnt != 0)
# 2272|                     for (i = old0; i < typecnt; i++)', true, 'Non-Issue', 'The checker is broken.  Bool promotes to int just fine.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (135, 'glibc-2.39-2.el10', 28, 'Error: CPPCHECK_WARNING (CWE-686):
glibc-2.39/timezone/zic.c:2274: error[invalidFunctionArgBool]: Invalid putc() argument nr 1. A non-boolean value is required.
# 2272|   		  for (i = old0; i < typecnt; i++)
# 2273|   			if (!omittype[i])
# 2274|-> 				putc(ttisuts[i], fp);
# 2275|   	}
# 2276|   	fprintf(fp, "\n%s\n", string);', true, 'Non-Issue', 'The checker is broken. Bool promotes to int just fine.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (182, 'glibc-2.39-2.el10', 75, 'Error: UNINIT (CWE-457):
glibc-2.39/benchtests/bench-pthread-lock-base.c:53: var_decl: Declaring variable "buf2" without initializer.
glibc-2.39/benchtests/bench-pthread-lock-base.c:55: uninit_use_in_call: Using uninitialized value "*buf2" when calling "memcpy". [Note: The source code implementation of the function has been overridden by a builtin model.]
#   53|     char buf1[512], buf2[512];
#   54|     int f = fibonacci (4);
#   55|->   memcpy (buf1, buf2, f);
#   56|   }
#   57|', true, 'Non-Issue', 'This function exists only to waste some time and block optimizations.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (136, 'glibc-2.39-2.el10', 29, 'Error: INTEGER_OVERFLOW (CWE-190):
glibc-2.39/elf/dl-map-segments.h:101: tainted_data_return: Called function "_dl_map_segment(c, mappref, maplength, fd)", and a possible return value is known to be less than zero.
glibc-2.39/elf/dl-map-segments.h:101: assign: Assigning: "l->l_map_start" = "_dl_map_segment(c, mappref, maplength, fd)".
glibc-2.39/elf/dl-map-segments.h:106: assign: Assigning: "l->l_addr" = "l->l_map_start - c->mapstart".
glibc-2.39/elf/dl-map-segments.h:156: overflow: The expression "l->l_addr + c->allocend" is considered to have possibly overflowed.
glibc-2.39/elf/dl-map-segments.h:156: assign: Assigning: "zeroend" = "l->l_addr + c->allocend".
glibc-2.39/elf/dl-map-segments.h:163: assign: Assigning: "zeropage" = "zeroend".
glibc-2.39/elf/dl-map-segments.h:176: overflow: The expression "zeropage - zero" is deemed overflowed because at least one of its arguments has overflowed.
glibc-2.39/elf/dl-map-segments.h:176: overflow_sink: "zeropage - zero", which might have underflowed, is passed to "memset((void *)zero, 0, zeropage - zero)". [Note: The source code implementation of the function has been overridden by a builtin model.]
#  174|                       return DL_MAP_SEGMENTS_ERROR_MPROTECT;
#  175|                   }
#  176|->               memset ((void *) zero, ''\0'', zeropage - zero);
#  177|                 if (__glibc_unlikely ((c->prot & PROT_WRITE) == 0))
#  178|                   __mprotect ((caddr_t) (zero & ~(GLRO(dl_pagesize) - 1)),', true, 'Non-Issue', 'The only negative value that can be returned is -1 (MAP_FAILED) and that''s tested for.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (137, 'glibc-2.39-2.el10', 30, 'Error: INTEGER_OVERFLOW (CWE-190):
glibc-2.39/iconv/iconv_charmap.c:496: tainted_data_argument: The check "actlen < maxlen" contains the tainted expression "actlen" which causes "maxlen" to be considered tainted.
glibc-2.39/iconv/iconv_charmap.c:522: overflow: The expression "maxlen + 32768UL" is deemed underflowed because at least one of its arguments has underflowed.
glibc-2.39/iconv/iconv_charmap.c:522: overflow_sink: "maxlen + 32768UL", which might have underflowed, is passed to "realloc(inbuf, maxlen + 32768UL)". [Note: The source code implementation of the function has been overridden by a builtin model.]
#  520|   
#  521|           /* Increase the buffer.  */
#  522|->         new_inbuf = (char *) realloc (inbuf, maxlen + 32768);
#  523|           if (new_inbuf == NULL)
#  524|             {', true, 'Non-Issue', 'realloc is limited to half of the address space per allocation; 38768U increments cannot underflow without hitting the size limitation first.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (138, 'glibc-2.39-2.el10', 31, 'Error: INTEGER_OVERFLOW (CWE-190):
glibc-2.39/iconv/iconv_prog.c:550: tainted_data_argument: The check "actlen < maxlen" contains the tainted expression "actlen" which causes "maxlen" to be considered tainted.
glibc-2.39/iconv/iconv_prog.c:576: overflow: The expression "maxlen + 32768UL" is deemed underflowed because at least one of its arguments has underflowed.
glibc-2.39/iconv/iconv_prog.c:576: overflow_sink: "maxlen + 32768UL", which might have underflowed, is passed to "realloc(inbuf, maxlen + 32768UL)". [Note: The source code implementation of the function has been overridden by a builtin model.]
#  574|   
#  575|   	/* Increase the buffer.  */
#  576|-> 	new_inbuf = (char *) realloc (inbuf, maxlen + 32768);
#  577|   	if (new_inbuf == NULL)
#  578|   	  {', true, 'Non-Issue', 'maxlen+32768 can''t "wrap under" because realloc() won''t succeed once the size exceeds half of size_t''s range.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (139, 'glibc-2.39-2.el10', 32, 'Error: INTEGER_OVERFLOW (CWE-190):
glibc-2.39/locale/programs/ld-numeric.c:308: underflow: The decrement operator on the unsigned variable "act" might result in an underflow.
glibc-2.39/locale/programs/ld-numeric.c:311: overflow_sink: "act", which might have underflowed, is passed to "xrealloc(grouping, act)".
#  309|   	      grouping[act++] = ''\0'';
#  310|   
#  311|-> 	      numeric->grouping = xrealloc (grouping, act);
#  312|   	      numeric->grouping_len = act;
#  313|   	    }', true, 'Non-Issue', 'check for act==1 in previous line prevents underflow', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (140, 'glibc-2.39-2.el10', 33, 'Error: INTEGER_OVERFLOW (CWE-190):
glibc-2.39/locale/programs/linereader.c:805: underflow: The decrement operator on the unsigned variable "lrb.act" might result in an underflow.
glibc-2.39/locale/programs/linereader.c:810: overflow_sink: "lrb.act", which might have underflowed, is passed to "addc(&lrb, ''\0'')".
#  808|   	lr_error (lr, _("unterminated string"));
#  809|   
#  810|->       addc (&lrb, ''\0'');
#  811|       }
#  812|     else', true, 'Non-Issue', 'condition preceeding this line prevents underflow', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (141, 'glibc-2.39-2.el10', 34, 'Error: INTEGER_OVERFLOW (CWE-190):
glibc-2.39/resolv/res_send.c:747: tainted_data_return: Called function "read(statp->_vcsock, (char *)cp, (int)len)", and a possible return value may be less than zero.
glibc-2.39/resolv/res_send.c:747: cast_overflow: An assign that casts to a different type, which might trigger an overflow.
glibc-2.39/resolv/res_send.c:749: overflow: The expression "len -= n" might be negative, but is used in a context that treats it as unsigned.
glibc-2.39/resolv/res_send.c:747: overflow_sink: "(int)len", which might be negative, is passed to "read(statp->_vcsock, (char *)cp, (int)len)". [Note: The source code implementation of the function has been overridden by a builtin model.]
#  745|   
#  746|   	cp = *thisansp;
#  747|-> 	while (len != 0 && (n = read(statp->_vcsock, (char *)cp, (int)len)) > 0){
#  748|   		cp += n;
#  749|   		len -= n;', true, 'Non-Issue', 'there''s a check for read returning > 0 in the conditional', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (142, 'glibc-2.39-2.el10', 35, 'Error: INTEGER_OVERFLOW (CWE-190):
glibc-2.39/resolv/res_send.c:764: tainted_data_return: Called function "read(statp->_vcsock, junk, ((len > 512UL) ? 512UL : len))", and a possible return value may be less than zero.
glibc-2.39/resolv/res_send.c:764: cast_overflow: An assign that casts to a different type, which might trigger an overflow.
glibc-2.39/resolv/res_send.c:767: overflow: The expression "len -= n" might be negative, but is used in a context that treats it as unsigned.
glibc-2.39/resolv/res_send.c:767: overflow: The expression "len -= n" is deemed underflowed because at least one of its arguments has underflowed.
glibc-2.39/resolv/res_send.c:764: overflow_sink: "(len > 512UL) ? 512UL : len", which might have underflowed, is passed to "read(statp->_vcsock, junk, ((len > 512UL) ? 512UL : len))". [Note: The source code implementation of the function has been overridden by a builtin model.]
#  762|   			char junk[PACKETSZ];
#  763|   
#  764|-> 			n = read(statp->_vcsock, junk,
#  765|   				 (len > sizeof junk) ? sizeof junk : len);
#  766|   			if (n > 0)', true, 'Non-Issue', 'The value returned from read() will either be nonpositive (error) or no greater than the number of bytes requested.  Such limit prevents "n" from being more than "len" or less than one, so subtracting it from len cannot make len become negative.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (143, 'glibc-2.39-2.el10', 36, 'Error: INTEGER_OVERFLOW (CWE-190):
glibc-2.39/support/support_copy_file_range.c:81: tainted_data_return: Called function "read(infd, buf, to_read)", and a possible return value may be less than zero.
glibc-2.39/support/support_copy_file_range.c:81: assign: Assigning: "read_count" = "read(infd, buf, to_read)".
glibc-2.39/support/support_copy_file_range.c:140: overflow: The expression "length -= read_count" might be negative, but is used in a context that treats it as unsigned.
glibc-2.39/support/support_copy_file_range.c:140: overflow: The expression "length -= read_count" is deemed underflowed because at least one of its arguments has underflowed.
glibc-2.39/support/support_copy_file_range.c:74: assign: Assigning: "to_read" = "length".
glibc-2.39/support/support_copy_file_range.c:81: overflow_sink: "to_read", which might have underflowed, is passed to "read(infd, buf, to_read)". [Note: The source code implementation of the function has been overridden by a builtin model.]
#   79|         ssize_t read_count;
#   80|         if (pinoff == NULL)
#   81|-> 	read_count = read (infd, buf, to_read);
#   82|         else
#   83|   	read_count = pread64 (infd, buf, to_read, *pinoff);', true, 'Non-Issue', 'negative return value is checked for on line 87', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (144, 'glibc-2.39-2.el10', 37, 'Error: INTEGER_OVERFLOW (CWE-190):
glibc-2.39/time/tzfile.c:678: underflow: The decrement operator on the unsigned variable "i" might result in an underflow.
glibc-2.39/time/tzfile.c:711: deref_overflow: "i", which might have underflowed, is passed to "__tzname[types[type_idxs[i - 1UL]].isdst]".
#  709|   	  /* assert (timer >= transitions[i - 1]
#  710|   	     && (i == num_transitions || timer < transitions[i])); */
#  711|-> 	  __tzname[types[type_idxs[i - 1]].isdst]
#  712|   	    = __tzstring (&zone_names[types[type_idxs[i - 1]].idx]);
#  713|   	  size_t j = i;', true, 'Non-Issue', 'Lines 668/669 limit i to 0..num_transitions-1.  transitions[0] is known to be > timer (line 594) so loop on 667 will terminate before i is zero', 'The unsigned variable ''i'' cannot underflow in a way that causes a dereference overflow on line 711, as the while loop (lines 677-680) would terminate due to the comparison `timer < transitions[i - 1]` becoming false if ''i'' were to wrap around, ensuring ''i'' remains within bounds as implied by the assertion (lines 709-710).', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (145, 'glibc-2.39-2.el10', 38, 'Error: INTEGER_OVERFLOW (CWE-190):
glibc-2.39/time/tzfile.c:678: underflow: The decrement operator on the unsigned variable "i" might result in an underflow.
glibc-2.39/time/tzfile.c:711: overflow: The expression "i - 1UL" is deemed underflowed because at least one of its arguments has underflowed.
glibc-2.39/time/tzfile.c:711: deref_overflow: "i - 1UL", which might have underflowed, is passed to "__tzname[types[type_idxs[i - 1UL]].isdst]".
#  709|   	  /* assert (timer >= transitions[i - 1]
#  710|   	     && (i == num_transitions || timer < transitions[i])); */
#  711|-> 	  __tzname[types[type_idxs[i - 1]].isdst]
#  712|   	    = __tzstring (&zone_names[types[type_idxs[i - 1]].idx]);
#  713|   	  size_t j = i;', true, 'Non-Issue', 'The conditional on line 674 precludes the conditions that lead to underflow', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (146, 'glibc-2.39-2.el10', 39, 'Error: OVERRUN (CWE-119):
glibc-2.39/benchtests/bench-rawmemchr.c:34: overrun-buffer-arg: Calling "memchr" with "s" and "9223372036854775807UL" is suspicious because of the very large index, 9223372036854775807. The index may be due to a negative parameter being interpreted as unsigned.
#   32|   {
#   33|     if ((unsigned char) c != 0)
#   34|->     return memchr (s, c, PTRDIFF_MAX);
#   35|     return (char *)s + strlen (s);
#   36|   }', true, 'Non-Issue', 'This function is only called in a context where the needle is known to be findable.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (147, 'glibc-2.39-2.el10', 40, 'Error: OVERRUN (CWE-119):
glibc-2.39/benchtests/bench-strlen.c:42: overrun-buffer-arg: Calling "memchr" with "p" and "9223372036854775807UL" is suspicious because of the very large index, 9223372036854775807. The index may be due to a negative parameter being interpreted as unsigned.
#   40|   memchr_strlen (const CHAR *p)
#   41|   {
#   42|->   return (const CHAR *)MEMCHR (p, 0, PTRDIFF_MAX) - p;
#   43|   }
#   44|', true, 'Non-Issue', 'This function is only called in a context where the needle is known to be findable.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (148, 'glibc-2.39-2.el10', 41, 'Error: OVERRUN (CWE-119):
glibc-2.39/benchtests/bench-strlen.c:42: overrun-buffer-arg: Calling "wmemchr" with "p" and "9223372036854775807UL" is suspicious because of the very large index, 9223372036854775807. The index may be due to a negative parameter being interpreted as unsigned.
#   40|   memchr_strlen (const CHAR *p)
#   41|   {
#   42|->   return (const CHAR *)MEMCHR (p, 0, PTRDIFF_MAX) - p;
#   43|   }
#   44|', true, 'Issue', 'This function is only called in a context where the needle is known to be findable.', 'The `memchr_strlen` function at line 42 invokes `wmemchr` with `PTRDIFF_MAX` (equivalent to the suspicious large index `9223372036854775807UL`) without explicit bounds checking, potentially leading to a buffer overrun for buffers smaller than `PTRDIFF_MAX` bytes.', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (149, 'glibc-2.39-2.el10', 42, 'Error: OVERRUN (CWE-119):
glibc-2.39/elf/dl-cache.c:449: overrun-buffer-arg: Overrunning array "cache_new->magic" of 17 bytes by passing it to a function which accesses it at byte offset 19 using argument "20UL".
#  447|   
#  448|   	  cache_new = (struct cache_file_new *) ((void *) cache + offset);
#  449|-> 	  if (cachesize < (offset + sizeof (struct cache_file_new))
#  450|   	      || memcmp (cache_new->magic, CACHEMAGIC_VERSION_NEW,
#  451|   			 sizeof CACHEMAGIC_VERSION_NEW - 1) != 0)', true, 'Non-Issue', 'Your parser is faulty.  "sizeof FOO - 1" computes "sizeof FOO" first, then subtracts 1, resulting in an argument of 17UL.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (150, 'glibc-2.39-2.el10', 43, 'Error: OVERRUN (CWE-119):
glibc-2.39/locale/programs/locfile.c:370: strlen_assign: Setting variable "len" to the return value of strlen called with argument "output_path".
glibc-2.39/locale/programs/locfile.c:371: alloc_strlen: Allocating insufficient memory for the terminating null of the string.
#  369|     /* Remove trailing slashes and trailing pathname component.  */
#  370|     len = strlen (output_path);
#  371|->   base = (char *) alloca (len);
#  372|     memcpy (base, output_path, len);
#  373|     p = base + len;', true, 'Non-Issue', 'Line 383 fills in a trailing NUL, but will always decrement p at least once first, so there is guaranteed to be enough space.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (183, 'glibc-2.39-2.el10', 76, 'Error: UNINIT (CWE-457):
glibc-2.39/build-x86_64-redhat-linux/intl/plural.c:1031: var_decl: Declaring variable "yylval" without initializer.
glibc-2.39/build-x86_64-redhat-linux/intl/plural.c:1477: uninit_use: Using uninitialized value "yylval".
# 1475|   
# 1476|     YY_IGNORE_MAYBE_UNINITIALIZED_BEGIN
# 1477|->   *++yyvsp = yylval;
# 1478|     YY_IGNORE_MAYBE_UNINITIALIZED_END
# 1479|', true, 'Non-Issue', 'yylex initializes yylval around line 1182', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (151, 'glibc-2.39-2.el10', 44, 'Error: OVERRUN (CWE-119):
glibc-2.39/malloc/malloc.c:3879: cond_at_least: Checking "nb == 0UL" implies that "nb" is at least 1 on the false branch.
glibc-2.39/malloc/malloc.c:3977: cond_at_least: Checking "(unsigned long)nb < 1024UL" implies that "nb" is at least 1024 on the false branch.
glibc-2.39/malloc/malloc.c:4039: cond_between: Checking "((unsigned long)nb >> 6) <= 48UL" implies that "nb" is between 1024 and 3135 (inclusive) on the true branch.
glibc-2.39/malloc/malloc.c:4059: assignment: Assigning: "tc_idx" = "(nb - 32UL + 16UL - 1UL) / 16UL". The value of "tc_idx" is now between 62 and 194 (inclusive).
glibc-2.39/malloc/malloc.c:4140: overrun-local: Overrunning array "tcache->counts" of 64 2-byte elements at element index 194 (byte offset 389) using index "tc_idx" (which evaluates to 194).
# 4138|   	      /* Fill cache first, return to user only if cache fills.
# 4139|   		 We may return one of these chunks later.  */
# 4140|-> 	      if (tcache_nb > 0
# 4141|   		  && tcache->counts[tc_idx] < mp_.tcache_count)
# 4142|   		{', true, 'Non-Issue', 'Line 4060 sets tcache_nb if tc_idx is within its valid range; tcache_nb is checked on line 4141 to prevent out of range behavior.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (152, 'glibc-2.39-2.el10', 45, 'Error: OVERRUN (CWE-119):
glibc-2.39/malloc/malloc.c:3879: cond_at_least: Checking "nb == 0UL" implies that "nb" is at least 1 on the false branch.
glibc-2.39/malloc/malloc.c:3977: cond_at_least: Checking "(unsigned long)nb < 1024UL" implies that "nb" is at least 1024 on the false branch.
glibc-2.39/malloc/malloc.c:4039: cond_between: Checking "((unsigned long)nb >> 6) <= 48UL" implies that "nb" is between 1024 and 3135 (inclusive) on the true branch.
glibc-2.39/malloc/malloc.c:4059: assignment: Assigning: "tc_idx" = "(nb - 32UL + 16UL - 1UL) / 16UL". The value of "tc_idx" is now between 62 and 194 (inclusive).
glibc-2.39/malloc/malloc.c:4143: overrun-call: Overrunning callee''s array of size 64 by passing argument "tc_idx" (which evaluates to 194) in call to "tcache_put".
# 4141|   		  && tcache->counts[tc_idx] < mp_.tcache_count)
# 4142|   		{
# 4143|-> 		  tcache_put (victim, tc_idx);
# 4144|   		  return_cached = 1;
# 4145|   		  continue;', true, 'Non-Issue', 'Line 4060 sets tcache_nb if tc_idx is within its valid range; tcache_nb is checked on line 4141 to prevent out of range behavior.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (153, 'glibc-2.39-2.el10', 46, 'Error: OVERRUN (CWE-119):
glibc-2.39/malloc/malloc.c:3879: cond_at_least: Checking "nb == 0UL" implies that "nb" is at least 1 on the false branch.
glibc-2.39/malloc/malloc.c:3977: cond_at_least: Checking "(unsigned long)nb < 1024UL" implies that "nb" is at least 1024 on the false branch.
glibc-2.39/malloc/malloc.c:4039: cond_between: Checking "((unsigned long)nb >> 6) <= 48UL" implies that "nb" is between 1024 and 3135 (inclusive) on the true branch.
glibc-2.39/malloc/malloc.c:4059: assignment: Assigning: "tc_idx" = "(nb - 32UL + 16UL - 1UL) / 16UL". The value of "tc_idx" is now between 62 and 194 (inclusive).
glibc-2.39/malloc/malloc.c:4248: overrun-call: Overrunning callee''s array of size 64 by passing argument "tc_idx" (which evaluates to 194) in call to "tcache_get".
# 4246|         if (return_cached)
# 4247|   	{
# 4248|-> 	  return tcache_get (tc_idx);
# 4249|   	}
# 4250|   #endif', true, 'Non-Issue', 'return_cached is only set when previous tests ensure that the index is in range.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (154, 'glibc-2.39-2.el10', 47, 'Error: OVERRUN (CWE-119):
glibc-2.39/nss/nss_compat/compat-grp.c:303: strlen_assign: Setting variable "len" to the return value of strlen called with argument "result->gr_name".
glibc-2.39/nss/nss_compat/compat-grp.c:304: alloc_strlen: Allocating insufficient memory for the terminating null of the string.
#  302|   	{
#  303|   	  size_t len = strlen (result->gr_name);
#  304|-> 	  char buf[len];
#  305|   	  enum nss_status status;
#  306|', true, 'Non-Issue', 'the value copied in starts at the second character of the string, not the first, leaving room for the NUL', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (155, 'glibc-2.39-2.el10', 48, 'Error: OVERRUN (CWE-119):
glibc-2.39/nss/nss_compat/compat-grp.c:571: strlen_assign: Setting variable "len" to the return value of strlen called with argument "result->gr_name".
glibc-2.39/nss/nss_compat/compat-grp.c:572: alloc_strlen: Allocating insufficient memory for the terminating null of the string.
#  570|   	  /* Yes, no +1, see the memcpy call below.  */
#  571|   	  size_t len = strlen (result->gr_name);
#  572|-> 	  char buf[len];
#  573|   	  enum nss_status status;
#  574|', true, 'Non-Issue', 'the value copied in starts at the second character of the string, not the first, leaving room for the NUL', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (156, 'glibc-2.39-2.el10', 49, 'Error: OVERRUN (CWE-119):
glibc-2.39/nss/nss_compat/compat-pwd.c:1011: strlen_assign: Setting variable "len" to the return value of strlen called with argument "result->pw_name".
glibc-2.39/nss/nss_compat/compat-pwd.c:1012: alloc_strlen: Allocating insufficient memory for the terminating null of the string.
# 1010|   	{
# 1011|   	  size_t len = strlen (result->pw_name);
# 1012|-> 	  char buf[len];
# 1013|   	  enum nss_status status;
# 1014|', true, 'Non-Issue', 'the value copied in starts at the second character of the string, not the first, leaving room for the NUL', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (157, 'glibc-2.39-2.el10', 50, 'Error: OVERRUN (CWE-119):
glibc-2.39/nss/nss_compat/compat-pwd.c:583: strlen_assign: Setting variable "len" to the return value of strlen called with argument "result->pw_name".
glibc-2.39/nss/nss_compat/compat-pwd.c:584: alloc_strlen: Allocating insufficient memory for the terminating null of the string.
#  582|   	{
#  583|   	  size_t len = strlen (result->pw_name);
#  584|-> 	  char buf[len];
#  585|   	  enum nss_status status;
#  586|', true, 'Non-Issue', 'the value copied in starts at the second character of the string, not the first, leaving room for the NUL', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (158, 'glibc-2.39-2.el10', 51, 'Error: OVERRUN (CWE-119):
glibc-2.39/nss/nss_compat/compat-pwd.c:994: strlen_assign: Setting variable "len" to the return value of strlen called with argument "result->pw_name".
glibc-2.39/nss/nss_compat/compat-pwd.c:995: alloc_strlen: Allocating insufficient memory for the terminating null of the string.
#  993|   	{
#  994|   	  size_t len = strlen (result->pw_name);
#  995|-> 	  char buf[len];
#  996|   	  enum nss_status status;
#  997|', true, 'Non-Issue', 'buf doesn''t hold a C string, it holds bytes.  The trailing NUL is never stored.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (464, 'texinfo-7.1-2.el10', 19, 'Error: OVERRUN (CWE-119):
texinfo-7.1/info/session.c:2337: alloc_strlen: Allocating insufficient memory for the terminating null of the string. [Note: The source code implementation of the function has been overridden by a builtin model.]
# 2335|             if (defentry)
# 2336|               {
# 2337|->               prompt = xmalloc (strlen (defentry->label)
# 2338|                                   + strlen (_("Menu item (%s): ")));
# 2339|                 sprintf (prompt, _("Menu item (%s): "), defentry->label);', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-43595

', NULL, '2025-11-18 16:18:56.203498');
INSERT INTO public.ground_truth VALUES (160, 'glibc-2.39-2.el10', 53, 'Error: OVERRUN (CWE-119):
glibc-2.39/posix/glob.c:508: strlen_assign: Setting variable "dirlen" to the return value of strlen called with argument "pattern".
glibc-2.39/posix/glob.c:717: strlen_assign: Setting variable "home_len" to the return value of strlen called with argument "home_dir".
glibc-2.39/posix/glob.c:720: alloc_strlen: Allocating insufficient memory for the terminating null of the string.
#  718|                 int use_alloca = glob_use_alloca (alloca_used, home_len + dirlen);
#  719|                 if (use_alloca)
#  720|->                 newp = alloca_account (home_len + dirlen, alloca_used);
#  721|                 else
#  722|                   {', true, 'Non-Issue', 'mempcpy at line 733 starts at the *second* character of dirname, which causes the NUL at the end of dirname to be included within the allocated memory', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (161, 'glibc-2.39-2.el10', 54, 'Error: OVERRUN (CWE-119):
glibc-2.39/posix/glob.c:508: strlen_assign: Setting variable "dirlen" to the return value of strlen called with argument "pattern".
glibc-2.39/posix/glob.c:717: strlen_assign: Setting variable "home_len" to the return value of strlen called with argument "home_dir".
glibc-2.39/posix/glob.c:723: alloc_strlen: Allocating insufficient memory for the terminating null of the string. [Note: The source code implementation of the function has been overridden by a builtin model.]
#  721|                 else
#  722|                   {
#  723|->                   newp = malloc (home_len + dirlen);
#  724|                     if (newp == NULL)
#  725|                       {', true, 'Non-Issue', 'mempcpy at line 733 starts at the *second* character of dirname, which causes the NUL at the end of dirname to be included within the allocated memory', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (162, 'glibc-2.39-2.el10', 55, 'Error: OVERRUN (CWE-119):
glibc-2.39/sysdeps/posix/getcwd.c:244: assignment: Assigning: "allocated" = "4096UL".
glibc-2.39/sysdeps/posix/getcwd.c:429: cond_at_most: Checking "allocated > namlen" implies that "namlen" may be up to 4095 on the true branch.
glibc-2.39/sysdeps/posix/getcwd.c:443: overrun-buffer-arg: Overrunning array "d->d_name" of 256 bytes by passing it to a function which accesses it at byte offset 4094 using argument "namlen" (which evaluates to 4095). [Note: The source code implementation of the function has been overridden by a builtin model.]
#  441|           }
#  442|         dirp -= namlen;
#  443|->       memcpy (dirp, d->d_name, namlen);
#  444|         *--dirp = ''/'';
#  445|', true, 'Non-Issue', 'line 415 limits copy to size of d->d_name, and buffer is resized if it''s smaller in the large clause following.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (163, 'glibc-2.39-2.el10', 56, 'Error: OVERRUN (CWE-119):
glibc-2.39/sysdeps/unix/sysv/linux/getsysstats.c:113: alias: Assigning: "buffer_end" = "buffer + buffer_size". "buffer_end" now points to byte 1024 of "buffer" (which consists of 1024 bytes).
glibc-2.39/sysdeps/unix/sysv/linux/getsysstats.c:114: alias: Assigning: "cp" = "buffer_end". "cp" now points to byte 1024 of "buffer" (which consists of 1024 bytes).
glibc-2.39/sysdeps/unix/sysv/linux/getsysstats.c:123: overrun-local: Overrunning array of 1024 bytes at byte offset 1024 by dereferencing pointer "cp".
#  121|       {
#  122|         char *l;
#  123|->       while ((l = next_line (fd, buffer, &cp, &re, buffer_end)) != NULL)
#  124|   	/* The current format of /proc/stat has all the cpu* entries
#  125|   	   at the front.  We assume here that stays this way.  */', true, 'Non-Issue', 're and cp always point between buffer and buffer_end, access to *cp is limited by re-cp, so no real access happens until after at least line 38 when those pointers are changed.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (164, 'glibc-2.39-2.el10', 57, 'Error: OVERRUN (CWE-119):
glibc-2.39/sysdeps/unix/sysv/linux/getsysstats.c:142: alias: Assigning: "buffer_end" = "buffer + buffer_size". "buffer_end" now points to byte 1024 of "buffer" (which consists of 1024 bytes).
glibc-2.39/sysdeps/unix/sysv/linux/getsysstats.c:143: alias: Assigning: "cp" = "buffer_end". "cp" now points to byte 1024 of "buffer" (which consists of 1024 bytes).
glibc-2.39/sysdeps/unix/sysv/linux/getsysstats.c:153: overrun-local: Overrunning array of 1024 bytes at byte offset 1024 by dereferencing pointer "cp".
#  151|     if (fd != -1)
#  152|       {
#  153|->       l = next_line (fd, buffer, &cp, &re, buffer_end);
#  154|         if (l != NULL)
#  155|   	do', true, 'Non-Issue', 're and cp always point between buffer and buffer_end, access to *cp is limited by re-cp, so no real access happens until after at least line 38 when those pointers are changed.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (165, 'glibc-2.39-2.el10', 58, 'Error: OVERRUN (CWE-119):
glibc-2.39/sysdeps/unix/sysv/linux/procutils.c:79: alias: Assigning: "buffer_end" = "buffer + buffer_size". "buffer_end" now points to byte 256 of "buffer" (which consists of 256 bytes).
glibc-2.39/sysdeps/unix/sysv/linux/procutils.c:80: alias: Assigning: "cp" = "buffer_end". "cp" now points to byte 256 of "buffer" (which consists of 256 bytes).
glibc-2.39/sysdeps/unix/sysv/linux/procutils.c:90: overrun-local: Overrunning array of 256 bytes at byte offset 256 by dereferencing pointer "cp".
#   88|     char *l;
#   89|     int r;
#   90|->   while ((r = next_line (&l, fd, buffer, &cp, &re, buffer_end)) > 0)
#   91|       if (closure (l, arg) != 0)
#   92|         break;', true, 'Non-Issue', 're and cp always point between buffer and buffer_end, access to *cp is limited by re-cp, so no real access happens until after at least line 38 when those pointers are changed.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (166, 'glibc-2.39-2.el10', 59, 'Error: OVERRUN (CWE-119):
glibc-2.39/time/strptime_l.c:347: cond_at_most: Checking "cnt < 7" implies that "cnt" may be up to 6 on the true branch.
glibc-2.39/time/strptime_l.c:359: overrun-local: Overrunning array of 1 8-byte elements at element index 6 (byte offset 55) by dereferencing pointer "&_nl_C_LC_TIME.values[7].string + cnt".
#  357|   		      rp_longest = trp;
#  358|   		      cnt_longest = cnt;
#  359|-> 		      if (s.decided == not
#  360|   			  && strcmp (_NL_CURRENT (LC_TIME, DAY_1 + cnt),
#  361|   				     weekday_name[cnt]))', true, 'Non-Issue', 'This code is weird; the data has an array of values, each of which has one string - but they''re sequential, so accessing it as an array actually accesses the strings in sequential values. The data it''s accessing indeed has 12 string values starting at offset 111', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (219, 'graphite2-1.3.14-15.el10', 4, 'Error: RESOURCE_LEAK (CWE-772):
graphite2-1.3.14/src/Pass.cpp:275: alloc_fn: Storage is returned from allocation function "realloc".
graphite2-1.3.14/src/Pass.cpp:275: var_assign: Assigning: "moved_progs" = storage returned from "realloc(this->m_progs, prog_pool_free - this->m_progs)".
graphite2-1.3.14/src/Pass.cpp:280: leaked_storage: Variable "moved_progs" going out of scope leaks the storage it points to.
#  278|           free(m_progs);
#  279|           m_progs = 0;
#  280|->         return face.error(e);
#  281|       }
#  282|', true, 'Non-Issue', 'moved_progs variable is getting used in next lines of code.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.106182');
INSERT INTO public.ground_truth VALUES (167, 'glibc-2.39-2.el10', 60, 'Error: OVERRUN (CWE-119):
glibc-2.39/time/strptime_l.c:347: cond_at_most: Checking "cnt < 7" implies that "cnt" may be up to 6 on the true branch.
glibc-2.39/time/strptime_l.c:370: overrun-local: Overrunning array of 1 8-byte elements at element index 6 (byte offset 55) by dereferencing pointer "&_nl_C_LC_TIME.values[0].string + cnt".
#  368|   		      rp_longest = trp;
#  369|   		      cnt_longest = cnt;
#  370|-> 		      if (s.decided == not
#  371|   			  && strcmp (_NL_CURRENT (LC_TIME, ABDAY_1 + cnt),
#  372|   				     ab_weekday_name[cnt]))', true, 'Non-Issue', 'This code is weird; the data has an array of values, each of which has one string - but they''re sequential, so accessing it as an array actually accesses the strings in sequential values. The data it''s accessing indeed has 12 string values starting at offset 111', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (168, 'glibc-2.39-2.el10', 61, 'Error: OVERRUN (CWE-119):
glibc-2.39/time/strptime_l.c:347: cond_at_most: Checking "cnt < 7" implies that "cnt" may be up to 6 on the true branch.
glibc-2.39/time/strptime_l.c:378: overrun-local: Overrunning array of 1 8-byte elements at element index 6 (byte offset 55) by dereferencing pointer "&_nl_C_LC_TIME.values[7].string + cnt".
#  376|   #endif
#  377|   	      if (s.decided != loc
#  378|-> 		  && (((trp = rp, match_string (weekday_name[cnt], trp))
#  379|   		       && trp > rp_longest)
#  380|   		      || ((trp = rp, match_string (ab_weekday_name[cnt], rp))', true, 'Non-Issue', 'This code is weird; the data has an array of values, each of which has one string - but they''re sequential, so accessing it as an array actually accesses the strings in sequential values. The data it''s accessing indeed has 7 string values starting at offset 111', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (169, 'glibc-2.39-2.el10', 62, 'Error: OVERRUN (CWE-119):
glibc-2.39/time/strptime_l.c:347: cond_at_most: Checking "cnt < 7" implies that "cnt" may be up to 6 on the true branch.
glibc-2.39/time/strptime_l.c:380: overrun-local: Overrunning array of 1 8-byte elements at element index 6 (byte offset 55) by dereferencing pointer "&_nl_C_LC_TIME.values[0].string + cnt".
#  378|   		  && (((trp = rp, match_string (weekday_name[cnt], trp))
#  379|   		       && trp > rp_longest)
#  380|-> 		      || ((trp = rp, match_string (ab_weekday_name[cnt], rp))
#  381|   			  && trp > rp_longest)))
#  382|   		{', true, 'Non-Issue', 'This code is weird; the data has an array of values, each of which has one string - but they''re sequential, so accessing it as an array actually accesses the strings in sequential values. The data it''s accessing indeed has 7 string values starting at offset 111', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (170, 'glibc-2.39-2.el10', 63, 'Error: OVERRUN (CWE-119):
glibc-2.39/time/strptime_l.c:403: cond_at_most: Checking "cnt < 12" implies that "cnt" may be up to 11 on the true branch.
glibc-2.39/time/strptime_l.c:415: overrun-local: Overrunning array of 1 8-byte elements at element index 11 (byte offset 95) by dereferencing pointer "&_nl_C_LC_TIME.values[26].string + cnt".
#  413|   		      rp_longest = trp;
#  414|   		      cnt_longest = cnt;
#  415|-> 		      if (s.decided == not
#  416|   			  && strcmp (_NL_CURRENT (LC_TIME, MON_1 + cnt),
#  417|   				     month_name[cnt]))', true, 'Non-Issue', 'This code is weird; the data has an array of values, each of which has one string - but they''re sequential, so accessing it as an array actually accesses the strings in sequential values. The data it''s accessing indeed has 12 string values starting at offset 111', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (171, 'glibc-2.39-2.el10', 64, 'Error: OVERRUN (CWE-119):
glibc-2.39/time/strptime_l.c:403: cond_at_most: Checking "cnt < 12" implies that "cnt" may be up to 11 on the true branch.
glibc-2.39/time/strptime_l.c:426: overrun-local: Overrunning array of 1 8-byte elements at element index 11 (byte offset 95) by dereferencing pointer "&_nl_C_LC_TIME.values[14].string + cnt".
#  424|   		      rp_longest = trp;
#  425|   		      cnt_longest = cnt;
#  426|-> 		      if (s.decided == not
#  427|   			  && strcmp (_NL_CURRENT (LC_TIME, ABMON_1 + cnt),
#  428|   				     ab_month_name[cnt]))', true, 'Non-Issue', 'This code is weird; the data has an array of values, each of which has one string - but they''re sequential, so accessing it as an array actually accesses the strings in sequential values. The data it''s accessing indeed has 12 string values starting at offset 111', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (172, 'glibc-2.39-2.el10', 65, 'Error: OVERRUN (CWE-119):
glibc-2.39/time/strptime_l.c:403: cond_at_most: Checking "cnt < 12" implies that "cnt" may be up to 11 on the true branch.
glibc-2.39/time/strptime_l.c:439: overrun-local: Overrunning array of 1 8-byte elements at element index 11 (byte offset 95) by dereferencing pointer "&_nl_C_LC_TIME.values[111].string + cnt".
#  437|   		      rp_longest = trp;
#  438|   		      cnt_longest = cnt;
#  439|-> 		      if (s.decided == not
#  440|   			  && strcmp (_NL_CURRENT (LC_TIME, ALTMON_1 + cnt),
#  441|   				     alt_month_name[cnt]))', true, 'Non-Issue', 'This code is weird; the data has an array of values, each of which has one string - but they''re sequential, so accessing it as an array actually accesses the strings in sequential values. The data it''s accessing indeed has 12 string values starting at offset 111', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (173, 'glibc-2.39-2.el10', 66, 'Error: OVERRUN (CWE-119):
glibc-2.39/time/strptime_l.c:403: cond_at_most: Checking "cnt < 12" implies that "cnt" may be up to 11 on the true branch.
glibc-2.39/time/strptime_l.c:451: overrun-local: Overrunning array of 1 8-byte elements at element index 11 (byte offset 95) by dereferencing pointer "&_nl_C_LC_TIME.values[111].string + cnt".
#  449|   		      rp_longest = trp;
#  450|   		      cnt_longest = cnt;
#  451|-> 		      if (s.decided == not
#  452|   			  && strcmp (_NL_CURRENT (LC_TIME, _NL_ABALTMON_1 + cnt),
#  453|   				     alt_month_name[cnt]))', true, 'Non-Issue', 'This code is weird; the data has an array of values, each of which has one string - but they''re sequential, so accessing it as an array actually accesses the strings in sequential values. The data it''s accessing indeed has 7 string values starting at offset 111', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (174, 'glibc-2.39-2.el10', 67, 'Error: OVERRUN (CWE-119):
glibc-2.39/time/strptime_l.c:403: cond_at_most: Checking "cnt < 12" implies that "cnt" may be up to 11 on the true branch.
glibc-2.39/time/strptime_l.c:460: overrun-local: Overrunning array of 1 8-byte elements at element index 11 (byte offset 95) by dereferencing pointer "&_nl_C_LC_TIME.values[26].string + cnt".
#  458|   #endif
#  459|   	      if (s.decided != loc
#  460|-> 		  && (((trp = rp, match_string (month_name[cnt], trp))
#  461|   		       && trp > rp_longest)
#  462|   		      || ((trp = rp, match_string (ab_month_name[cnt], trp))', true, 'Non-Issue', 'This code is weird; the data has an array of values, each of which has one string - but they''re sequential, so accessing it as an array actually accesses the strings in sequential values. The data it''s accessing indeed has 7 string values starting at offset 111', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (175, 'glibc-2.39-2.el10', 68, 'Error: OVERRUN (CWE-119):
glibc-2.39/time/strptime_l.c:403: cond_at_most: Checking "cnt < 12" implies that "cnt" may be up to 11 on the true branch.
glibc-2.39/time/strptime_l.c:462: overrun-local: Overrunning array of 1 8-byte elements at element index 11 (byte offset 95) by dereferencing pointer "&_nl_C_LC_TIME.values[14].string + cnt".
#  460|   		  && (((trp = rp, match_string (month_name[cnt], trp))
#  461|   		       && trp > rp_longest)
#  462|-> 		      || ((trp = rp, match_string (ab_month_name[cnt], trp))
#  463|   			  && trp > rp_longest)
#  464|   #ifdef _LIBC', true, 'Non-Issue', 'This code is weird; the data has an array of values, each of which has one string - but they''re sequential, so accessing it as an array actually accesses the strings in sequential values. The data it''s accessing indeed has 7 string values starting at offset 111', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (176, 'glibc-2.39-2.el10', 69, 'Error: OVERRUN (CWE-119):
glibc-2.39/time/strptime_l.c:403: cond_at_most: Checking "cnt < 12" implies that "cnt" may be up to 11 on the true branch.
glibc-2.39/time/strptime_l.c:465: overrun-local: Overrunning array of 1 8-byte elements at element index 11 (byte offset 95) by dereferencing pointer "&_nl_C_LC_TIME.values[111].string + cnt".
#  463|   			  && trp > rp_longest)
#  464|   #ifdef _LIBC
#  465|-> 		      || ((trp = rp, match_string (alt_month_name[cnt], trp))
#  466|   			  && trp > rp_longest)
#  467|   		      || ((trp = rp, match_string (ab_alt_month_name[cnt], trp))', true, 'Non-Issue', 'This code is weird; the data has an array of values, each of which has one string - but they''re sequential, so accessing it as an array actually accesses the strings in sequential values.  The data it''s accessing indeed has 12 string values starting at offset 111', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (177, 'glibc-2.39-2.el10', 70, 'Error: OVERRUN (CWE-119):
glibc-2.39/time/strptime_l.c:403: cond_at_most: Checking "cnt < 12" implies that "cnt" may be up to 11 on the true branch.
glibc-2.39/time/strptime_l.c:467: overrun-local: Overrunning array of 1 8-byte elements at element index 11 (byte offset 95) by dereferencing pointer "&_nl_C_LC_TIME.values[135].string + cnt".
#  465|   		      || ((trp = rp, match_string (alt_month_name[cnt], trp))
#  466|   			  && trp > rp_longest)
#  467|-> 		      || ((trp = rp, match_string (ab_alt_month_name[cnt], trp))
#  468|   			  && trp > rp_longest)
#  469|   #endif', true, 'Non-Issue', 'This code is weird; the data has an array of values, each of which has one string - but they''re sequential, so accessing it as an array actually accesses the strings in sequential values. The data it''s accessing indeed has 12 string values starting at offset 111', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (178, 'glibc-2.39-2.el10', 71, 'Error: RESOURCE_LEAK (CWE-772):
glibc-2.39/benchtests/bench-strchr.c:183: alloc_fn: Storage is returned from allocation function "xmalloc". [Note: The source code implementation of the function has been overridden by a builtin model.]
glibc-2.39/benchtests/bench-strchr.c:183: var_assign: Assigning: "impl_array" = storage returned from "xmalloc((impl_count + func_count) * 24UL)".
glibc-2.39/benchtests/bench-strchr.c:183: var_assign: Assigning: "a" = "impl_array".
glibc-2.39/benchtests/bench-strchr.c:183: leaked_storage: Variable "a" going out of scope leaks the storage it points to.
glibc-2.39/benchtests/bench-strchr.c:183: var_assign: Assigning: "impl" = "impl_array".
glibc-2.39/benchtests/bench-strchr.c:188: leaked_storage: Variable "impl" going out of scope leaks the storage it points to.
glibc-2.39/benchtests/bench-strchr.c:199: overwrite_var: Overwriting "impl_array" in "impl_array = xmalloc((impl_count + func_count) * 24UL)" leaks the storage that "impl_array" points to.
#  197|       json_array_begin (json_ctx, "timings");
#  198|   
#  199|->     FOR_EACH_IMPL (impl, 0)
#  200|         do_one_rand_test (json_ctx, impl, buf + align, c);
#  201|', true, 'Non-Issue', 'This only happens once, and this is a standalone benchmark program, so it''s irrelevent', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (179, 'glibc-2.39-2.el10', 72, 'Error: RESOURCE_LEAK (CWE-772):
glibc-2.39/benchtests/bench-strstr.c:314: alloc_fn: Storage is returned from allocation function "xmalloc". [Note: The source code implementation of the function has been overridden by a builtin model.]
glibc-2.39/benchtests/bench-strstr.c:314: var_assign: Assigning: "impl_array" = storage returned from "xmalloc((impl_count + func_count) * 24UL)".
glibc-2.39/benchtests/bench-strstr.c:314: var_assign: Assigning: "a" = "impl_array".
glibc-2.39/benchtests/bench-strstr.c:314: leaked_storage: Variable "a" going out of scope leaks the storage it points to.
glibc-2.39/benchtests/bench-strstr.c:314: var_assign: Assigning: "impl" = "impl_array".
glibc-2.39/benchtests/bench-strstr.c:319: leaked_storage: Variable "impl" going out of scope leaks the storage it points to.
glibc-2.39/benchtests/bench-strstr.c:349: overwrite_var: Overwriting "impl_array" in "impl_array = xmalloc((impl_count + func_count) * 24UL)" leaks the storage that "impl_array" points to.
#  347|       json_array_begin (json_ctx, "timings");
#  348|   
#  349|->     FOR_EACH_IMPL (impl, 0)
#  350|         do_one_test (json_ctx, impl, hs, ne, NULL);
#  351|', true, 'Non-Issue', 'This only happens once, and this is a standalone benchmark program, so it''s irrelevent', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (180, 'glibc-2.39-2.el10', 73, 'Error: REVERSE_NEGATIVE (CWE-191):
glibc-2.39/sysdeps/ieee754/dbl-64/k_rem_pio2.c:297: negative_sink: Using "jz" as index to array "iq".
glibc-2.39/sysdeps/ieee754/dbl-64/k_rem_pio2.c:306: check_after_sink: You might be using variable "jz" before verifying that it is >= 0.
#  304|        full precision (this function is not called for zero arguments).
#  305|        Help the compiler to know it.  */
#  306|->   if (jz < 0) __builtin_unreachable ();
#  307|   
#  308|     /* convert integer "bit" chunk to floating-point value */', true, 'Non-Issue', 'As per the comment prior to this line, this comparison is known to be false at this point, and the code exists only to silence the very error you are reporting.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (181, 'glibc-2.39-2.el10', 74, 'Error: UNINIT (CWE-457):
glibc-2.39/argp/argp-help.c:465: alloc_fn: Calling "malloc" which returns uninitialized memory. [Note: The source code implementation of the function has been overridden by a builtin model.]
glibc-2.39/argp/argp-help.c:465: assign: Assigning: "hol->short_options" = "malloc(num_short_options + 1U)", which points to uninitialized data.
glibc-2.39/argp/argp-help.c:491: uninit_use_in_call: Using uninitialized value "*hol->short_options" when calling "find_char".
#  489|   	    {
#  490|   	      entry->num++;
#  491|-> 	      if (oshort (o) && ! find_char (o->key, hol->short_options, so))
#  492|   		/* O has a valid short option which hasn''t already been used.*/
#  493|   		*so++ = o->key;', true, 'Non-Issue', 'also passes "so" which points to the boundary between initialized data and uninitialized.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (184, 'glibc-2.39-2.el10', 77, 'Error: UNINIT (CWE-457):
glibc-2.39/build-x86_64-redhat-linux/intl/plural.c:1052: var_decl: Declaring variable "yyvsa" without initializer.
glibc-2.39/build-x86_64-redhat-linux/intl/plural.c:1053: assign: Assigning: "yyvs" = "yyvsa", which points to uninitialized data.
glibc-2.39/build-x86_64-redhat-linux/intl/plural.c:1054: assign: Assigning: "yyvsp" = "yyvs", which points to uninitialized data.
glibc-2.39/build-x86_64-redhat-linux/intl/plural.c:1269: uninit_use: Using uninitialized value "yyvsp[1 - yylen]".
# 1267|        unconditionally makes the parser a bit smaller, and it avoids a
# 1268|        GCC warning that YYVAL may be used uninitialized.  */
# 1269|->   yyval = yyvsp[1-yylen];
# 1270|   
# 1271|', true, 'Non-Issue', 'yyval is intentionally set to uninitizlied data in the cases where the following switch statement will initialize it, to avoid warning that the value is uninitialized, as the comment says.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (185, 'glibc-2.39-2.el10', 78, 'Error: UNINIT (CWE-457):
glibc-2.39/build-x86_64-redhat-linux/intl/plural.c:1052: var_decl: Declaring variable "yyvsa" without initializer.
glibc-2.39/build-x86_64-redhat-linux/intl/plural.c:1053: assign: Assigning: "yyvs" = "yyvsa", which points to uninitialized data.
glibc-2.39/build-x86_64-redhat-linux/intl/plural.c:1143: uninit_use_in_call: Using uninitialized value "*yyvs" when calling "__builtin_memcpy".
# 1141|             YYNOMEM;
# 1142|           YYSTACK_RELOCATE (yyss_alloc, yyss);
# 1143|->         YYSTACK_RELOCATE (yyvs_alloc, yyvs);
# 1144|   #  undef YYSTACK_RELOCATE
# 1145|           if (yyss1 != yyssa)', true, 'Non-Issue', '(1) this code is part of bison, not glibc, and (2) this code is merely replacing a too-small buffer with a larger buffer; the logic for avoiding using uninitialized memory applies to the new buffer as much as it applied to the old.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (186, 'glibc-2.39-2.el10', 79, 'Error: UNINIT (CWE-457):
glibc-2.39/catgets/gencat.c:852: var_decl: Declaring variable "string_pool" without initializer.
glibc-2.39/catgets/gencat.c:974: assign: Assigning: "__o" = "&string_pool", which points to uninitialized data.
glibc-2.39/catgets/gencat.c:974: uninit_use_in_call: Using uninitialized value "__o->extra_arg" when calling "_obstack_newchunk".
#  972|   	  /* Add current string to the continuous space containing all
#  973|   	     strings.  */
#  974|-> 	  obstack_grow0 (&string_pool, message_run->message,
#  975|   			 strlen (message_run->message));
#  976|', true, 'Non-Issue', 'call to obstack_init in line 950 makes the data initialized.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (187, 'glibc-2.39-2.el10', 80, 'Error: UNINIT (CWE-457):
glibc-2.39/elf/dl-find_object.c:794: alloc_fn: Calling "malloc" which returns uninitialized memory. [Note: The source code implementation of the function has been overridden by a builtin model.]
glibc-2.39/elf/dl-find_object.c:794: assign: Assigning: "map_array" = "malloc(count * 8UL)", which points to uninitialized data.
glibc-2.39/elf/dl-find_object.c:804: uninit_use_in_call: Using uninitialized value "*map_array" when calling "_dl_find_object_link_map_sort".
#  802|     }
#  803|   
#  804|->   _dl_find_object_link_map_sort (map_array, count);
#  805|     bool ok = _dl_find_object_update_1 (map_array, count);
#  806|     free (map_array);', true, 'Non-Issue', 'map_array is initialized in the loop at line 797', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (188, 'glibc-2.39-2.el10', 81, 'Error: UNINIT (CWE-457):
glibc-2.39/elf/dl-fini.c:68: var_decl: Declaring variable "maps" without initializer.
glibc-2.39/elf/dl-fini.c:94: uninit_use_in_call: Using uninitialized value "*maps" when calling "_dl_sort_maps".
#   92|   	     binary itself which is at the front of the search list for
#   93|   	     the main namespace.  */
#   94|-> 	  _dl_sort_maps (maps, nmaps, (ns == LM_ID_BASE), true);
#   95|   
#   96|   	  /* We do not rely on the linked list of loaded object anymore', true, 'Non-Issue', 'maps is initialized via line 79, which counts how many entries are usable into nmaps.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (189, 'glibc-2.39-2.el10', 82, 'Error: UNINIT (CWE-457):
glibc-2.39/locale/programs/ld-collate.c:2109: var_decl: Declaring variable "weightpool" without initializer.
glibc-2.39/locale/programs/ld-collate.c:2165: assign: Assigning: "__o" = "&weightpool", which points to uninitialized data.
glibc-2.39/locale/programs/ld-collate.c:2165: uninit_use_in_call: Using uninitialized value "__o->extra_arg" when calling "_obstack_newchunk".
# 2163|   	int j;
# 2164|   
# 2165|-> 	obstack_make_room (&weightpool, nrules);
# 2166|   
# 2167|   	for (j = 0; j < nrules; ++j)', true, 'Non-Issue', 'obstack_init() initialized it.  extra_arg is only used if use_extra_arg is set, which only happens when extra_arg is set.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (190, 'glibc-2.39-2.el10', 83, 'Error: UNINIT (CWE-457):
glibc-2.39/locale/programs/ld-collate.c:2109: var_decl: Declaring variable "weightpool" without initializer.
glibc-2.39/locale/programs/ld-collate.c:2175: assign: Assigning: "__o" = "&weightpool", which points to uninitialized data.
glibc-2.39/locale/programs/ld-collate.c:2175: uninit_use_in_call: Using uninitialized value "__o->extra_arg" when calling "_obstack_newchunk".
# 2173|     if (i > 0)
# 2174|       do
# 2175|->       obstack_1grow (&weightpool, ''\0'');
# 2176|       while (++i < LOCFILE_ALIGN);
# 2177|', true, 'Non-Issue', 'obstack_init() initialized it. extra_arg is only used if use_extra_arg is set, which only happens when extra_arg is set.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (191, 'glibc-2.39-2.el10', 84, 'Error: UNINIT (CWE-457):
glibc-2.39/locale/programs/ld-collate.c:2109: var_decl: Declaring variable "weightpool" without initializer.
glibc-2.39/locale/programs/ld-collate.c:2352: assign: Assigning: "__o" = "&weightpool", which points to uninitialized data.
glibc-2.39/locale/programs/ld-collate.c:2352: uninit_use_in_call: Using uninitialized value "__o->extra_arg" when calling "_obstack_newchunk".
# 2350|     /* Add padding to the tables if necessary.  */
# 2351|     while (!LOCFILE_ALIGNED_P (obstack_object_size (&weightpool)))
# 2352|->     obstack_1grow (&weightpool, 0);
# 2353|   
# 2354|     /* Now add the four tables.  */', true, 'Non-Issue', 'obstack_init() initialized it. extra_arg is only used if use_extra_arg is set, which only happens when extra_arg is set.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (364, 'sqlite-3.45.1-2.el10', 33, 'Error: OVERRUN (CWE-119):
sqlite-src-3450100/sqlite3.c:77642: cond_const: Checking "k < 6" implies that "k" is 6 on the false branch.
sqlite-src-3450100/sqlite3.c:77643: overrun-local: Overrunning array "pCArray->apEnd" of 6 8-byte elements at element index 6 (byte offset 55) using index "k" (which evaluates to 6).
#77641|   
#77642|     for(k=0; ALWAYS(k<NB*2) && pCArray->ixNx[k]<=i; k++){}
#77643|->   pSrcEnd = pCArray->apEnd[k];
#77644|   
#77645|     pData = pEnd;', true, 'Non-Issue', 'As per upstream this is not a bug:
https://sqlite.org/forum/forumpost/d0e144c233c7b286 
Upstream also added some asserts to future releases so that static analysers are not confused.
In my opinion these asserts do not need to be backported.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (192, 'glibc-2.39-2.el10', 85, 'Error: UNINIT (CWE-457):
glibc-2.39/locale/programs/ld-collate.c:2110: var_decl: Declaring variable "extrapool" without initializer.
glibc-2.39/locale/programs/ld-collate.c:2309: assign: Assigning: "__o" = "&extrapool", which points to uninitialized data.
glibc-2.39/locale/programs/ld-collate.c:2309: uninit_use_in_call: Using uninitialized value "__o->extra_arg" when calling "_obstack_newchunk".
# 2307|   					  + runp->nmbs - 1);
# 2308|   		assert (LOCFILE_ALIGNED_P (obstack_object_size (&extrapool)));
# 2309|-> 		obstack_make_room (&extrapool, added);
# 2310|   
# 2311|   		obstack_int32_grow_fast (&extrapool, weightidx);', true, 'Non-Issue', 'obstack_init() initialized it. extra_arg is only used if use_extra_arg is set, which only happens when extra_arg is set.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (193, 'glibc-2.39-2.el10', 86, 'Error: UNINIT (CWE-457):
glibc-2.39/misc/tsearch.c:337: alloc_fn: Calling "malloc" which returns uninitialized memory. [Note: The source code implementation of the function has been overridden by a builtin model.]
glibc-2.39/misc/tsearch.c:337: assign: Assigning: "q" = "(struct node_t *)malloc(24UL)", which points to uninitialized data.
glibc-2.39/misc/tsearch.c:351: uninit_use: Using uninitialized value "q->left_node".
#  349|         SETNODEPTR(nextp,q);		/* link new node to old */
#  350|         q->key = key;			/* initialize new node */
#  351|->       SETRED(q);
#  352|         SETLEFT(q,NULL);
#  353|         SETRIGHT(q,NULL);', true, 'Non-Issue', 'SETRED does not access q->left_node.  Those three macros initialize q.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (194, 'glibc-2.39-2.el10', 87, 'Error: UNINIT (CWE-457):
glibc-2.39/nss/getaddrinfo.c:2034: var_decl: Declaring variable "endp" without initializer.
glibc-2.39/nss/getaddrinfo.c:2066: uninit_use: Using uninitialized value "endp".
# 2064|   		    }
# 2065|   		}
# 2066|-> 	      else if (inet_pton (AF_INET, val1, &prefix.s6_addr32[3])
# 2067|   		       && (cp == NULL
# 2068|   			   || (bits = strtoul (cp, &endp, 10)) != ULONG_MAX', true, 'Non-Issue', 'endp is initialized by strtoul() on line 2068', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (195, 'glibc-2.39-2.el10', 88, 'Error: UNINIT (CWE-457):
glibc-2.39/posix/regexec.c:1396: alloc_fn: Calling "malloc" which returns uninitialized memory. [Note: The source code implementation of the function has been overridden by a builtin model.]
glibc-2.39/posix/regexec.c:1396: assign: Assigning: "fs->stack" = "(struct re_fail_stack_ent_t *)malloc(fs->alloc * 32UL)", which points to uninitialized data.
glibc-2.39/posix/regexec.c:1409: uninit_use_in_call: Using uninitialized value "fs->stack->eps_via_nodes.elems" when calling "free_fail_stack_return".
glibc-2.39/posix/regexec.c:1409: uninit_use_in_call: Using uninitialized value "fs->stack->regs" when calling "free_fail_stack_return".
# 1407|       {
# 1408|         regmatch_list_free (&prev_match);
# 1409|->       free_fail_stack_return (fs);
# 1410|         return REG_ESPACE;
# 1411|       }', true, 'Non-Issue', 'fs->num limits access to the stack; memory will not be accessed until it is initialized and num is incremented.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (196, 'glibc-2.39-2.el10', 89, 'Error: UNINIT (CWE-457):
glibc-2.39/posix/regexec.c:1396: alloc_fn: Calling "malloc" which returns uninitialized memory. [Note: The source code implementation of the function has been overridden by a builtin model.]
glibc-2.39/posix/regexec.c:1396: assign: Assigning: "fs->stack" = "(struct re_fail_stack_ent_t *)malloc(fs->alloc * 32UL)", which points to uninitialized data.
glibc-2.39/posix/regexec.c:1429: uninit_use_in_call: Using uninitialized value "fs->stack->regs" when calling "pop_fail_stack".
glibc-2.39/posix/regexec.c:1429: uninit_use_in_call: Using uninitialized value "fs->stack->eps_via_nodes" when calling "pop_fail_stack".
glibc-2.39/posix/regexec.c:1429: uninit_use_in_call: Using uninitialized value "fs->stack->idx" when calling "pop_fail_stack".
glibc-2.39/posix/regexec.c:1429: uninit_use_in_call: Using uninitialized value "fs->stack->node" when calling "pop_fail_stack".
# 1427|   		if (pmatch[reg_idx].rm_so > -1 && pmatch[reg_idx].rm_eo == -1)
# 1428|   		  {
# 1429|-> 		    cur_node = pop_fail_stack (fs, &idx, nmatch, pmatch,
# 1430|   					       prev_idx_match, &eps_via_nodes);
# 1431|   		    break;', true, 'Non-Issue', 'fs->num limits the amount of fs->stack that''s accessed to only those entries that are initialized via push_fail_stack()', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (197, 'glibc-2.39-2.el10', 90, 'Error: UNINIT (CWE-457):
glibc-2.39/posix/wordexp.c:2212: var_decl: Declaring variable "ifs_white" without initializer.
glibc-2.39/posix/wordexp.c:2267: assign: Assigning: "runp" = "ifs_white", which points to uninitialized data.
glibc-2.39/posix/wordexp.c:2269: uninit_use: Using uninitialized value "*runp".
# 2267|   	      char *runp = ifs_white;
# 2268|   
# 2269|-> 	      while (runp < whch && *runp != *ifsch)
# 2270|   		++runp;
# 2271|', true, 'Non-Issue', 'whch tracks which parts of ifwhite are initialized and which aren''t', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (198, 'glibc-2.39-2.el10', 91, 'Error: UNINIT (CWE-457):
glibc-2.39/resolv/getaddrinfo_a.c:39: var_decl: Declaring variable "defsigev" without initializer.
glibc-2.39/resolv/getaddrinfo_a.c:55: assign: Assigning: "sig" = "&defsigev", which points to uninitialized data.
glibc-2.39/resolv/getaddrinfo_a.c:173: uninit_use: Using uninitialized value "*sig". Field "sig->sigev_value" is uninitialized.
#  171|   
#  172|   	  waitlist->counter = total;
#  173|-> 	  waitlist->sigev = *sig;
#  174|   	}
#  175|       }', true, 'Non-Issue', 'defsigev.sigev_notify = SIGEV_NONE prevents access to the uninitialized portions', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (199, 'glibc-2.39-2.el10', 92, 'Error: UNINIT (CWE-457):
glibc-2.39/resolv/getaddrinfo_a.c:39: var_decl: Declaring variable "defsigev" without initializer.
glibc-2.39/resolv/getaddrinfo_a.c:55: assign: Assigning: "sig" = "&defsigev", which points to uninitialized data.
glibc-2.39/resolv/getaddrinfo_a.c:90: uninit_use_in_call: Using uninitialized value "sig->_sigev_un" when calling "__gai_notify_only".
#   88|   
#   89|         if (mode == GAI_NOWAIT)
#   90|-> 	__gai_notify_only (sig,
#   91|   			   sig->sigev_notify == SIGEV_SIGNAL ? getpid () : 0);
#   92|', true, 'Non-Issue', 'gai_notify_only will only access sig''s other data when sig->sigev_notify is not SIGEV_NONE, and defsigev''s is SIGEV_NONE, which protects the other fields from being accessed.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (389, 'sqlite-3.45.1-2.el10', 58, 'Error: UNINIT (CWE-457):
sqlite-src-3450100/sqlite3.c:204919: skipped_decl: Jumping over declaration of "seenE".
sqlite-src-3450100/sqlite3.c:205024: uninit_use: Using uninitialized value "seenE".
#205022|             }
#205023|           }
#205024|->         if( seenE ){
#205025|             pParse->iErr = j;
#205026|             return -1;', true, 'Non-Issue', 'seenE is set before entering the goto section.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (200, 'glibc-2.39-2.el10', 93, 'Error: UNINIT (CWE-457):
glibc-2.39/resolv/getaddrinfo_a.c:39: var_decl: Declaring variable "defsigev" without initializer.
glibc-2.39/resolv/getaddrinfo_a.c:55: assign: Assigning: "sig" = "&defsigev", which points to uninitialized data.
glibc-2.39/resolv/getaddrinfo_a.c:90: uninit_use_in_call: Using uninitialized value "sig->_sigev_un" when calling "__gai_notify_only".
glibc-2.39/resolv/getaddrinfo_a.c:90: uninit_use_in_call: Using uninitialized value "sig->sigev_value" when calling "__gai_notify_only".
#   88|   
#   89|         if (mode == GAI_NOWAIT)
#   90|-> 	__gai_notify_only (sig,
#   91|   			   sig->sigev_notify == SIGEV_SIGNAL ? getpid () : 0);
#   92|', true, 'Non-Issue', 'gai_notify_only will only access sig''s other data when sig->sigev_notify is not SIGEV_NONE, and defsigev''s is SIGEV_NONE, which protects the other fields from being accessed.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (201, 'glibc-2.39-2.el10', 94, 'Error: UNINIT (CWE-457):
glibc-2.39/resolv/res_send.c:804: var_decl: Declaring variable "slen" without initializer.
glibc-2.39/resolv/res_send.c:856: uninit_use_in_call: Using uninitialized value "slen" when calling "__connect".
#  854|   		DIAG_PUSH_NEEDS_COMMENT;
#  855|   		DIAG_IGNORE_Os_NEEDS_COMMENT (5, "-Wmaybe-uninitialized");
#  856|-> 		if (__connect (EXT (statp).nssocks[ns], nsap, slen) < 0) {
#  857|   		DIAG_POP_NEEDS_COMMENT;
#  858|   			__res_iclose(statp, false);', true, 'Non-Issue', 'whether slen is uninitialized or not correlates with whether EST(statp).nssocks[ns] is set on lines 808 and 815.  If slen is not initialized, the test on line 820 will be true and the function will exit.  Thus, slen will be initialized by line 856', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (202, 'glibc-2.39-2.el10', 95, 'Error: UNINIT (CWE-457):
glibc-2.39/rt/aio_suspend.c:119: var_decl: Declaring variable "requestlist" without initializer.
glibc-2.39/rt/aio_suspend.c:216: uninit_use: Using uninitialized value "requestlist[cnt]".
#  214|   	struct waitlist **listp;
#  215|   
#  216|-> 	assert (requestlist[cnt] != NULL);
#  217|   
#  218|   	/* There is the chance that we cannot find our entry anymore. This', true, 'Non-Issue', 'cnt limits access to entries initialized on line 138', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (203, 'glibc-2.39-2.el10', 96, 'Error: UNINIT (CWE-457):
glibc-2.39/rt/lio_listio-common.c:77: var_decl: Declaring variable "defsigev" without initializer.
glibc-2.39/rt/lio_listio-common.c:86: assign: Assigning: "sig" = "&defsigev", which points to uninitialized data.
glibc-2.39/rt/lio_listio-common.c:126: uninit_use_in_call: Using uninitialized value "sig->_sigev_un" when calling "__aio_notify_only".
#  124|   
#  125|         if (LIO_MODE (mode) == LIO_NOWAIT)
#  126|->         __aio_notify_only (sig);
#  127|   
#  128|         return result;', true, 'Non-Issue', '__aio_notify_only will only read the other members when the signal is other than SIGEV_NONE, and defsigev is SIGEV_NONE', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (204, 'glibc-2.39-2.el10', 97, 'Error: UNINIT (CWE-457):
glibc-2.39/rt/lio_listio-common.c:77: var_decl: Declaring variable "defsigev" without initializer.
glibc-2.39/rt/lio_listio-common.c:86: assign: Assigning: "sig" = "&defsigev", which points to uninitialized data.
glibc-2.39/rt/lio_listio-common.c:126: uninit_use_in_call: Using uninitialized value "sig->_sigev_un" when calling "__aio_notify_only".
glibc-2.39/rt/lio_listio-common.c:126: uninit_use_in_call: Using uninitialized value "sig->sigev_value" when calling "__aio_notify_only".
#  124|   
#  125|         if (LIO_MODE (mode) == LIO_NOWAIT)
#  126|-> 	__aio_notify_only (sig);
#  127|   
#  128|         return result;', true, 'Non-Issue', 'defsigev is partially initialized with SIGEV_NONE, which prevents __aio_notify_only from trying to access any other fields', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (205, 'glibc-2.39-2.el10', 98, 'Error: UNINIT (CWE-457):
glibc-2.39/rt/lio_listio-common.c:77: var_decl: Declaring variable "defsigev" without initializer.
glibc-2.39/rt/lio_listio-common.c:86: assign: Assigning: "sig" = "&defsigev", which points to uninitialized data.
glibc-2.39/rt/lio_listio-common.c:126: uninit_use_in_call: Using uninitialized value "sig->sigev_value" when calling "__aio_notify_only".
glibc-2.39/rt/lio_listio-common.c:126: uninit_use_in_call: Using uninitialized value "sig->sigev_signo" when calling "__aio_notify_only".
#  124|   
#  125|         if (LIO_MODE (mode) == LIO_NOWAIT)
#  126|-> 	__aio_notify_only (sig);
#  127|   
#  128|         return result;', true, 'Non-Issue', '__aio_notify_only will only read the other members when the signal is other than SIGEV_NONE, and defsigev is SIGEV_NONE', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (206, 'glibc-2.39-2.el10', 99, 'Error: UNINIT (CWE-457):
glibc-2.39/rt/lio_listio-common.c:77: var_decl: Declaring variable "defsigev" without initializer.
glibc-2.39/rt/lio_listio-common.c:86: assign: Assigning: "sig" = "&defsigev", which points to uninitialized data.
glibc-2.39/rt/lio_listio-common.c:221: uninit_use: Using uninitialized value "*sig". Field "sig->sigev_value" is uninitialized.
#  219|   
#  220|   	  waitlist->counter = total;
#  221|-> 	  waitlist->sigev = *sig;
#  222|   	}
#  223|       }', true, 'Non-Issue', 'defsigev is partially initialized with SIGEV_NONE, which prevents other code from trying to access any other fields', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (207, 'glibc-2.39-2.el10', 100, 'Error: UNINIT (CWE-457):
glibc-2.39/stdlib/strtod_l.c:519: var_decl: Declaring variable "den" without initializer.
glibc-2.39/stdlib/strtod_l.c:1466: uninit_use_in_call: Using uninitialized value "den[densize - 1L]" when calling "__builtin_clzll".
# 1464|        */
# 1465|   
# 1466|->     count_leading_zeros (cnt, den[densize - 1]);
# 1467|   
# 1468|       if (cnt > 0)', true, 'Non-Issue', 'den is initialized by the do loop at 1415 and memcpy at 1446', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (208, 'glibc-2.39-2.el10', 101, 'Error: UNINIT (CWE-457):
glibc-2.39/sunrpc/auth_des.c:137: alloc_fn: Calling "malloc" which returns uninitialized memory. [Note: The source code implementation of the function has been overridden by a builtin model.]
glibc-2.39/sunrpc/auth_des.c:137: assign: Assigning: "ad" = "(struct ad_private *)malloc(1160UL)", which points to uninitialized data.
glibc-2.39/sunrpc/auth_des.c:205: uninit_use: Using uninitialized value "ad->ad_fullname".
#  203|     if (ad != NULL)
#  204|       {
#  205|->       if (ad->ad_fullname != NULL)
#  206|   	FREE (ad->ad_fullname, ad->ad_fullnamelen + 1);
#  207|         if (ad->ad_servername != NULL)', true, 'Non-Issue', 'ad_fullname is set at line 150 and filled at line 164', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (465, 'texinfo-7.1-2.el10', 20, 'Error: CPPCHECK_WARNING (CWE-404):
texinfo-7.1/install-info/install-info.c:835: error[resourceLeak]: Resource leak: f2
#  833|         f2 = freopen (*opened_filename, FOPEN_RBIN, stdin);
#  834|         if (!f)
#  835|->         return 0;
#  836|         f = popen (command, "r");
#  837|         fclose (f2);', false, 'Non-Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-43595

', NULL, '2025-11-18 16:18:56.203498');
INSERT INTO public.ground_truth VALUES (209, 'glibc-2.39-2.el10', 102, 'Error: UNINIT (CWE-457):
glibc-2.39/sunrpc/auth_des.c:137: alloc_fn: Calling "malloc" which returns uninitialized memory. [Note: The source code implementation of the function has been overridden by a builtin model.]
glibc-2.39/sunrpc/auth_des.c:137: assign: Assigning: "ad" = "(struct ad_private *)malloc(1160UL)", which points to uninitialized data.
glibc-2.39/sunrpc/auth_des.c:207: uninit_use: Using uninitialized value "ad->ad_servername".
#  205|         if (ad->ad_fullname != NULL)
#  206|   	FREE (ad->ad_fullname, ad->ad_fullnamelen + 1);
#  207|->       if (ad->ad_servername != NULL)
#  208|   	FREE (ad->ad_servername, ad->ad_servernamelen + 1);
#  209|         FREE (ad, sizeof (struct ad_private));', true, 'Non-Issue', 'memory pointed to by ad is initialized by memset on line 145', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (210, 'glibc-2.39-2.el10', 103, 'Error: UNINIT (CWE-457):
glibc-2.39/support/support_subprocess.c:32: var_decl: Declaring variable "result" without initializer.
glibc-2.39/support/support_subprocess.c:45: uninit_use: Using uninitialized value "result". Field "result.pid" is uninitialized.
#   43|     TEST_VERIFY (fflush (stderr) == 0);
#   44|   
#   45|->   return result;
#   46|   }
#   47|', true, 'Non-Issue', 'All callers of support_subprocess_init set pid before returning.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (211, 'glibc-2.39-2.el10', 104, 'Error: UNINIT (CWE-457):
glibc-2.39/sysdeps/x86_64/dl-machine.h:435: skipped_decl: Jumping over declaration of "fmt".
glibc-2.39/sysdeps/x86_64/dl-machine.h:445: uninit_use_in_call: Using uninitialized value "fmt" when calling "_dl_error_printf".
#  443|   	      strtab = (const char *) D_PTR (map, l_info[DT_STRTAB]);
#  444|   
#  445|-> 	      _dl_error_printf (fmt, RTLD_PROGNAME, strtab + refsym->st_name);
#  446|   	    }
#  447|   	  break;', true, 'Non-Issue', 'declaration is in scope for all jumps to print_err, and fmt is set prior to each of those jumps', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (212, 'glibc-2.39-2.el10', 105, 'Error: UNINIT (CWE-457):
glibc-2.39/timezone/zic.c:2018: var_decl: Declaring variable "typemap" without initializer.
glibc-2.39/timezone/zic.c:2226: uninit_use: Using uninitialized value "typemap[types[i]]".
# 2224|   		  putc(currenttype, fp);
# 2225|   		for (i = thistimei; i < thistimelim; ++i) {
# 2226|-> 		  currenttype = typemap[types[i]];
# 2227|   		  putc(currenttype, fp);
# 2228|   		}', true, 'Non-Issue', 'typemap is initialized at line 2146', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (213, 'glibc-2.39-2.el10', 106, 'Error: UNINIT (CWE-457):
glibc-2.39/timezone/zic.c:2488: var_decl: Declaring variable "dstr" without initializer.
glibc-2.39/timezone/zic.c:2548: assign: Assigning: "dstrp" = "&dstr", which points to uninitialized data.
glibc-2.39/timezone/zic.c:2576: uninit_use_in_call: Using uninitialized value "dstrp->r_wday" when calling "stringrule".
# 2574|   	}
# 2575|   	result[len++] = '','';
# 2576|-> 	c = stringrule(result + len, dstrp, dstrp->r_save, zp->z_stdoff);
# 2577|   	if (c < 0) {
# 2578|   		result[0] = ''\0'';', true, 'Non-Issue', 'dstr.r_dycode is set to DC_DOM which avoids the conditional branch that uses r_wday', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (214, 'glibc-2.39-2.el10', 107, 'Error: UNINIT (CWE-457):
glibc-2.39/timezone/zic.c:2488: var_decl: Declaring variable "stdr" without initializer.
glibc-2.39/timezone/zic.c:2549: assign: Assigning: "stdrp" = "&stdr", which points to uninitialized data.
glibc-2.39/timezone/zic.c:2585: uninit_use_in_call: Using uninitialized value "stdrp->r_wday" when calling "stringrule".
# 2583|   	len += strlen(result + len);
# 2584|   	result[len++] = '','';
# 2585|-> 	c = stringrule(result + len, stdrp, dstrp->r_save, zp->z_stdoff);
# 2586|   	if (c < 0) {
# 2587|   		result[0] = ''\0'';', true, 'Non-Issue', 'stdr.r_dycode is set to DC_DOM which avoids the conditional branch that uses r_wday', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (215, 'glibc-2.39-2.el10', 108, 'Error: UNINIT (CWE-457):
glibc-2.39/timezone/zic.c:2602: var_decl: Declaring variable "starttime" without initializer.
glibc-2.39/timezone/zic.c:2749: uninit_use_in_call: Using uninitialized value "starttime" when calling "addtt".
# 2747|   				startttisut);
# 2748|   			if (usestart) {
# 2749|-> 				addtt(starttime, type);
# 2750|   				usestart = false;
# 2751|   			} else', true, 'Non-Issue', 'starttime is initialized on line 2631', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.074885');
INSERT INTO public.ground_truth VALUES (216, 'graphite2-1.3.14-15.el10', 1, 'Error: COMPILER_WARNING (CWE-483):
graphite2-1.3.14/tests/endian/endiantest.cpp: scope_hint: In function ‘int main(int, char**)’
graphite2-1.3.14/tests/endian/endiantest.cpp:141:5: warning[-Wmisleading-indentation]: this ‘if’ clause does not guard...
#  141 |     if   (!test_swaps<uint64, uint32, uint16, uint8>()
#      |     ^~
graphite2-1.3.14/tests/endian/endiantest.cpp:145:9: note: ...this statement, but the latter is misleadingly indented as if it were guarded by the ‘if’
#  145 |         if (r == 0) r = test_reads<uint64, uint32, uint16, uint8>(rounds);
#      |         ^~
#  139|   	int r = 0;
#  140|   
#  141|->     if   (!test_swaps<uint64, uint32, uint16, uint8>()
#  142|          || !test_swaps<int64, int32, int16, int8>())
#  143|           return 5;', true, 'Non-Issue', 'its just cosmetic issue of writing if statement', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.106182');
INSERT INTO public.ground_truth VALUES (217, 'graphite2-1.3.14-15.el10', 2, 'Error: CPPCHECK_WARNING (CWE-562):
graphite2-1.3.14/gr2fonttest/UtfCodec.h:197: error[returnDanglingLifetime]: Returning pointer that will be invalid when returning.
#  195|   
#  196|           reference         operator * () const throw() { return *this; }
#  197|->         pointer                operator ->() const throw() { return &operator *(); }
#  198|   
#  199|           operator codeunit_type * () const throw() { return cp; }', true, 'Non-Issue', 'I think this is not true, its not invalid return pointer', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.106182');
INSERT INTO public.ground_truth VALUES (218, 'graphite2-1.3.14-15.el10', 3, 'Error: RESOURCE_LEAK (CWE-772):
graphite2-1.3.14/src/GlyphCache.cpp:130: alloc_fn: Storage is returned from allocation function "operator new[]".
graphite2-1.3.14/src/GlyphCache.cpp:130: var_assign: Assigning: "glyphs" = storage returned from "new graphite2::GlyphFace[this->_num_glyphs]".
graphite2-1.3.14/src/GlyphCache.cpp:135: noescape: Resource "glyphs[0]" is not freed or pointed-to in "read_glyph".
graphite2-1.3.14/src/GlyphCache.cpp:171: leaked_storage: Variable "glyphs" going out of scope leaks the storage it points to.
#  169|   	// the dtor needs to call delete[] on _glyphs[0] to release what was allocated
#  170|   	// as glyphs
#  171|->     }
#  172|   
#  173|       if (_glyphs && glyph(0) == 0)', true, 'Non-Issue', 'calling read_glyph on index 0 saved glyphs as _glyphs[0]. Setting _glyph_loader to nullptr here flags that the dtor needs to call delete[] on _glyphs[0] to release what was allocated as glyphs', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.106182');
INSERT INTO public.ground_truth VALUES (220, 'gzip-1.13-1.el10', 1, 'Error: OVERRUN (CWE-119):
gzip-1.13/deflate.c:713: cond_at_least: Checking "lookahead < 262U" implies that "lookahead" is at least 262 on the false branch.
gzip-1.13/deflate.c:660: assignment: Assigning: "match_length" = "lookahead". The value of "match_length" is now at least 262.
gzip-1.13/deflate.c:665: overrun-call: Overrunning callee''s array of size 256 by passing argument "match_length - 3U" (which evaluates to 259) in call to "ct_tally".
#  663|               check_match(strstart, match_start, match_length);
#  664|   
#  665|->             flush = ct_tally(strstart-match_start, match_length - MIN_MATCH);
#  666|   
#  667|               lookahead -= match_length;', true, 'Non-Issue', 'Assuming this refers to the length_code[lc] in ct_tally function, the value of lc is checked by the asserts to be smaller than the array size', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.110298');
INSERT INTO public.ground_truth VALUES (221, 'gzip-1.13-1.el10', 2, 'Error: OVERRUN (CWE-119):
gzip-1.13/inflate.c:348: cond_const: Checking "j <= 16U" implies that "j" is 17 on the false branch.
gzip-1.13/inflate.c:353: assignment: Assigning: "l" = "j". The value of "l" is now 17.
gzip-1.13/inflate.c:354: assignment: Assigning: "i" = "16U".
gzip-1.13/inflate.c:354: decr: Decrementing "i". The value of "i" is now 15.
gzip-1.13/inflate.c:354: cond_at_least: Checking "i" implies that "i" is at least 1 on the true branch.
gzip-1.13/inflate.c:358: cond_at_least: Checking "(unsigned int)l > i" implies that "g" and "i" are at least 17 on the false branch.
gzip-1.13/inflate.c:369: overrun-local: Overrunning array "c" of 17 4-byte elements at element index 17 (byte offset 71) using index "i" (which evaluates to 17).
#  367|     if ((y -= c[i]) < 0)
#  368|       return 2;
#  369|->   c[i] += y;
#  370|   
#  371|', true, 'Non-Issue', 'The value of i is BMAX at most (line 354), the array c has BMAX+1 elements', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.110298');
INSERT INTO public.ground_truth VALUES (222, 'gzip-1.13-1.el10', 3, 'Error: USE_AFTER_FREE (CWE-416):
gzip-1.13/inflate.c:813: assign: Assigning: "td" = "tl + ((unsigned int)b & m)".
gzip-1.13/inflate.c:813: assign: Assigning: "td" = "tl + ((unsigned int)b & m)".
gzip-1.13/inflate.c:813: assign: Assigning: "td" = "tl + ((unsigned int)b & m)".
gzip-1.13/inflate.c:813: assign: Assigning: "td" = "tl + ((unsigned int)b & m)".
gzip-1.13/inflate.c:813: assign: Assigning: "td" = "tl + ((unsigned int)b & m)".
gzip-1.13/inflate.c:813: assign: Assigning: "td" = "tl + ((unsigned int)b & m)".
gzip-1.13/inflate.c:813: assign: Assigning: "td" = "tl + ((unsigned int)b & m)".
gzip-1.13/inflate.c:813: assign: Assigning: "td" = "tl + ((unsigned int)b & m)".
gzip-1.13/inflate.c:813: assign: Assigning: "td" = "tl + ((unsigned int)b & m)".
gzip-1.13/inflate.c:860: freed_arg: "huft_free" frees "tl".
gzip-1.13/inflate.c:897: pass_freed_arg: Passing freed pointer "td" as an argument to "inflate_codes".
#  895|     {
#  896|       /* decompress until an end-of-block code */
#  897|->     int err = inflate_codes(tl, td, bl, bd) ? 1 : 0;
#  898|   
#  899|       /* free the decoding tables */', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-44662

the line 860 seems like a bug -> send to upstream', 'td, derived from tl, is used in inflate_codes (line 897) after tl''s memory is freed by huft_free (line 860), potentially accessing freed memory, aligning with CWE-416 vulnerability, with no evident mitigation in the provided code.', '2025-11-18 16:18:56.110298');
INSERT INTO public.ground_truth VALUES (223, 'gzip-1.13-1.el10', 4, 'Error: OVERRUN (CWE-119):
gzip-1.13/deflate.c:771: cond_between: Checking "prev_length >= 3U" implies that "prev_length" is between 4 and 261 (inclusive) on the true branch.
gzip-1.13/deflate.c:775: overrun-call: Overrunning callee''s array of size 256 by passing argument "prev_length - 3U" (which evaluates to 258) in call to "ct_tally".
#  773|               check_match(strstart-1, prev_match, prev_length);
#  774|   
#  775|->             flush = ct_tally(strstart-1-prev_match, prev_length - MIN_MATCH);
#  776|   
#  777|               /* Insert in hash table all strings up to the end of the match.', true, 'Non-Issue', 'Assuming this refers to the length_code[lc] in ct_tally function, the value of lc is checked by the asserts to be smaller than the array size', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.110298');
INSERT INTO public.ground_truth VALUES (224, 'gzip-1.13-1.el10', 5, 'Error: OVERRUN (CWE-119):
gzip-1.13/inflate.c:348: cond_const: Checking "j <= 16U" implies that "j" is 17 on the false branch.
gzip-1.13/inflate.c:365: overrun-local: Overrunning array "c" of 17 4-byte elements at element index 17 (byte offset 71) using index "j" (which evaluates to 17).
#  363|     /* Adjust last length count to fill out codes, if needed */
#  364|     for (y = 1 << j; j < i; j++, y <<= 1)
#  365|->     if ((y -= c[j]) < 0)
#  366|         return 2;                 /* bad input: more codes than bits */
#  367|     if ((y -= c[i]) < 0)', true, 'Non-Issue', 'the condition of the for cycle makes sure that j < i; i is bmax at most (line 354); c has bmax+1 elements', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.110298');
INSERT INTO public.ground_truth VALUES (225, 'gzip-1.13-1.el10', 6, 'Error: OVERRUN (CWE-119):
gzip-1.13/inflate.c:348: cond_const: Checking "j <= 16U" implies that "j" is 17 on the false branch.
gzip-1.13/inflate.c:353: assignment: Assigning: "l" = "j". The value of "l" is now 17.
gzip-1.13/inflate.c:354: assignment: Assigning: "i" = "16U".
gzip-1.13/inflate.c:354: decr: Decrementing "i". The value of "i" is now 15.
gzip-1.13/inflate.c:354: cond_at_least: Checking "i" implies that "i" is at least 1 on the true branch.
gzip-1.13/inflate.c:357: assignment: Assigning: "g" = "i". The value of "g" is now at least 1.
gzip-1.13/inflate.c:358: cond_at_least: Checking "(unsigned int)l > i" implies that "g" and "i" are at least 17 on the false branch.
gzip-1.13/inflate.c:386: overrun-local: Overrunning array "x" of 17 4-byte elements at element index 17 (byte offset 71) using index "g" (which evaluates to 17).
#  384|         v[x[j]++] = i;
#  385|     } while (++i < n);
#  386|->   n = x[g];                   /* set n to length of v */
#  387|   
#  388|', true, 'Non-Issue', 'The value of g is BMAX at most (line 354/357), the array x has BMAX+1 elements', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.110298');
INSERT INTO public.ground_truth VALUES (233, 'libksba-1.6.5-3.el10', 1, 'Error: UNINIT (CWE-457):
libksba-1.6.5/src/asn1-parse.c:1520: var_decl: Declaring variable "yyvsa" without initializer.
libksba-1.6.5/src/asn1-parse.c:1521: assign: Assigning: "yyvs" = "yyvsa", which points to uninitialized data.
libksba-1.6.5/src/asn1-parse.c:1614: uninit_use_in_call: Using uninitialized value "*yyvs" when calling "__builtin_memcpy".
# 1612|             YYNOMEM;
# 1613|           YYSTACK_RELOCATE (yyss_alloc, yyss);
# 1614|->         YYSTACK_RELOCATE (yyvs_alloc, yyvs);
# 1615|   #  undef YYSTACK_RELOCATE
# 1616|           if (yyss1 != yyssa)', true, 'Non-Issue', 'generated code from bison has some workarouds for older compilers, but in this case they are likely ignored. I do not see a way this could be used uninitialized, even though it might not be completely clear to the checkers. Maybe also exclude with a subpath?', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.116994');
INSERT INTO public.ground_truth VALUES (226, 'gzip-1.13-1.el10', 7, 'Error: USE_AFTER_FREE (CWE-416):
gzip-1.13/inflate.c:813: assign: Assigning: "td" = "tl + ((unsigned int)b & m)".
gzip-1.13/inflate.c:813: assign: Assigning: "td" = "tl + ((unsigned int)b & m)".
gzip-1.13/inflate.c:813: assign: Assigning: "td" = "tl + ((unsigned int)b & m)".
gzip-1.13/inflate.c:813: assign: Assigning: "td" = "tl + ((unsigned int)b & m)".
gzip-1.13/inflate.c:813: assign: Assigning: "td" = "tl + ((unsigned int)b & m)".
gzip-1.13/inflate.c:813: assign: Assigning: "td" = "tl + ((unsigned int)b & m)".
gzip-1.13/inflate.c:813: assign: Assigning: "td" = "tl + ((unsigned int)b & m)".
gzip-1.13/inflate.c:813: assign: Assigning: "td" = "tl + ((unsigned int)b & m)".
gzip-1.13/inflate.c:813: assign: Assigning: "td" = "tl + ((unsigned int)b & m)".
gzip-1.13/inflate.c:860: freed_arg: "huft_free" frees "tl".
gzip-1.13/inflate.c:887: double_free: Calling "huft_free" frees pointer "td" which has already been freed.
#  885|       }
#  886|   #else
#  887|->       huft_free(td);
#  888|       }
#  889|       huft_free(tl);', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-44662

the line 860 seems like a bug -> send to upstream', 'Potential double-free vulnerability exists as `td` (assigned a `tl`-derived value at line 813) is freed at line 887 after `tl`''s freeing at line 860, with no explicit proof in the provided code that `td` doesn''t point within `tl`''s memory block, thus possibly rendering `td` a dangling pointer.', '2025-11-18 16:18:56.110298');
INSERT INTO public.ground_truth VALUES (227, 'gzip-1.13-1.el10', 8, 'Error: OVERRUN (CWE-119):
gzip-1.13/inflate.c:348: cond_const: Checking "j <= 16U" implies that "j" is 17 on the false branch.
gzip-1.13/inflate.c:353: assignment: Assigning: "l" = "j". The value of "l" is now 17.
gzip-1.13/inflate.c:354: assignment: Assigning: "i" = "16U".
gzip-1.13/inflate.c:354: decr: Decrementing "i". The value of "i" is now 15.
gzip-1.13/inflate.c:354: cond_at_least: Checking "i" implies that "i" is at least 1 on the true branch.
gzip-1.13/inflate.c:358: cond_at_least: Checking "(unsigned int)l > i" implies that "g" and "i" are at least 17 on the false branch.
gzip-1.13/inflate.c:367: overrun-local: Overrunning array "c" of 17 4-byte elements at element index 17 (byte offset 71) using index "i" (which evaluates to 17).
#  365|       if ((y -= c[j]) < 0)
#  366|         return 2;                 /* bad input: more codes than bits */
#  367|->   if ((y -= c[i]) < 0)
#  368|       return 2;
#  369|     c[i] += y;', true, 'Non-Issue', 'The value of i is BMAX at most (line 354), the array c has BMAX+1 elements', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.110298');
INSERT INTO public.ground_truth VALUES (228, 'gzip-1.13-1.el10', 9, 'Error: UNINIT (CWE-457):
gzip-1.13/inflate.c:300: var_decl: Declaring variable "r" without initializer.
gzip-1.13/inflate.c:471: uninit_use: Using uninitialized value "r". Field "r.v" is uninitialized.
#  469|         f = 1 << (k - w);
#  470|         for (j = i >> w; j < z; j += f)
#  471|->         q[j] = r;
#  472|   
#  473|         /* backwards increment the k-bit code i */', true, 'Non-Issue', 'the values are initialized by the if-else starting on line 454', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.110298');
INSERT INTO public.ground_truth VALUES (229, 'gzip-1.13-1.el10', 10, 'Error: USE_AFTER_FREE (CWE-416):
gzip-1.13/inflate.c:813: assign: Assigning: "td" = "tl + ((unsigned int)b & m)".
gzip-1.13/inflate.c:813: assign: Assigning: "td" = "tl + ((unsigned int)b & m)".
gzip-1.13/inflate.c:813: assign: Assigning: "td" = "tl + ((unsigned int)b & m)".
gzip-1.13/inflate.c:813: assign: Assigning: "td" = "tl + ((unsigned int)b & m)".
gzip-1.13/inflate.c:813: assign: Assigning: "td" = "tl + ((unsigned int)b & m)".
gzip-1.13/inflate.c:813: assign: Assigning: "td" = "tl + ((unsigned int)b & m)".
gzip-1.13/inflate.c:813: assign: Assigning: "td" = "tl + ((unsigned int)b & m)".
gzip-1.13/inflate.c:813: assign: Assigning: "td" = "tl + ((unsigned int)b & m)".
gzip-1.13/inflate.c:813: assign: Assigning: "td" = "tl + ((unsigned int)b & m)".
gzip-1.13/inflate.c:860: freed_arg: "huft_free" frees "tl".
gzip-1.13/inflate.c:897: deref_arg: Calling "inflate_codes" dereferences freed pointer "td".
#  895|     {
#  896|       /* decompress until an end-of-block code */
#  897|->     int err = inflate_codes(tl, td, bl, bd) ? 1 : 0;
#  898|   
#  899|       /* free the decoding tables */', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-44662

the line 860 seems like a bug -> send to upstream', 'Use-after-free vulnerability (CWE-416) exists as `td`, assigned a value based on freed `tl` (line 813), is dereferenced in `inflate_codes` (line 897) after `tl` is freed (line 860), with no intervening reassignment or reallocation of `td`''s underlying memory.', '2025-11-18 16:18:56.110298');
INSERT INTO public.ground_truth VALUES (230, 'gzip-1.13-1.el10', 11, 'Error: OVERRUN (CWE-119):
gzip-1.13/inflate.c:348: cond_const: Checking "j <= 16U" implies that "j" is 17 on the false branch.
gzip-1.13/inflate.c:351: assignment: Assigning: "k" = "j". The value of "k" is now 17.
gzip-1.13/inflate.c:401: overrun-local: Overrunning array "c" of 17 4-byte elements at element index 17 (byte offset 71) using index "k" (which evaluates to 17).
#  399|     for (; k <= g; k++)
#  400|     {
#  401|->     a = c[k];
#  402|       while (a--)
#  403|       {', true, 'Non-Issue', 'g is BMAX at most (lines 354+357), based on for-loop condition (399), k is also BMAX at most, the size of c is BMAX+1', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.110298');
INSERT INTO public.ground_truth VALUES (231, 'libconfig-1.7.3-8.el10', 1, 'Error: UNINIT (CWE-457):
libconfig-1.7.3/lib/grammar.c:1110: var_decl: Declaring variable "yylval" without initializer.
libconfig-1.7.3/lib/grammar.c:1802: uninit_use: Using uninitialized value "yylval".
# 1800|   
# 1801|     YY_IGNORE_MAYBE_UNINITIALIZED_BEGIN
# 1802|->   *++yyvsp = yylval;
# 1803|     YY_IGNORE_MAYBE_UNINITIALIZED_END
# 1804|', true, 'Non-Issue', 'YY_IGNORE_MAYBE_UNINITIALIZED_BEGIN', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.114521');
INSERT INTO public.ground_truth VALUES (232, 'libconfig-1.7.3-8.el10', 2, 'Error: OVERRUN (CWE-119):
libconfig-1.7.3/lib/grammar.c:1161: assignment: Assigning: "yystacksize" = "200UL".
libconfig-1.7.3/lib/grammar.c:1214: assignment: Assigning: "yystacksize" *= "2UL". The value of "yystacksize" is now 400.
libconfig-1.7.3/lib/grammar.c:1224: alias: Assigning: "yyss" = "&yyptr->yyss_alloc". "yyss" now points to element 0 of "yyptr->yyss_alloc" (which consists of 4 2-byte elements).
libconfig-1.7.3/lib/grammar.c:1239: illegal_address: "yyss + yystacksize - 1" evaluates to an address that is at byte offset 798 of an array of 8 bytes.
# 1237|                     (unsigned long int) yystacksize));
# 1238|   
# 1239|->       if (yyss + yystacksize - 1 <= yyssp)
# 1240|           YYABORT;
# 1241|       }', true, 'Non-Issue', 'memory not accessed, just comparing addresses', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.114521');
INSERT INTO public.ground_truth VALUES (318, 'nano-7.2-6.el10', 6, 'Error: USE_AFTER_FREE (CWE-672):
nano-7.2/src/files.c:1676: freed_arg: "copy_file" frees "backup_file".
nano-7.2/src/files.c:1683: use_closed_file: Calling "fclose" uses file handle "backup_file" after closing it.
# 1681|   		goto failure;
# 1682|   	} else if (verdict > 0) {
# 1683|-> 		fclose(backup_file);
# 1684|   		goto problem;
# 1685|   	}', true, 'Non-Issue', '`copy_file` copies `original` to `backup_file` and the third argument decides if `backup_file` should be automatically closed after the copying is done.  Since this flag is set to `FALSE`, `backup_file` is still open when `copy_file` returns and `fclose` is, therefore, called only once.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.152762');
INSERT INTO public.ground_truth VALUES (234, 'libksba-1.6.5-3.el10', 2, 'Error: OVERRUN (CWE-119):
libksba-1.6.5/src/asn1-parse.c:1512: assignment: Assigning: "yystacksize" = "200L".
libksba-1.6.5/src/asn1-parse.c:1602: assignment: Assigning: "yystacksize" *= "2L". The value of "yystacksize" is now 400.
libksba-1.6.5/src/asn1-parse.c:1613: alias: Assigning: "yyss" = "&yyptr->yyss_alloc". "yyss" now points to byte 0 of "yyptr->yyss_alloc" (which consists of 136 bytes).
libksba-1.6.5/src/asn1-parse.c:1629: illegal_address: "yyss + yystacksize - 1" evaluates to an address that is at byte offset 399 of an array of 136 bytes.
# 1627|         YY_IGNORE_USELESS_CAST_END
# 1628|   
# 1629|->       if (yyss + yystacksize - 1 <= yyssp)
# 1630|           YYABORT;
# 1631|       }', true, 'Non-Issue', 'generated code from bison has some workarouds for older compilers, but in this case they are likely ignored. I do not see a way this could be used uninitialized, even though it might not be completely clear to the checkers. Maybe also exclude with a subpath?', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.116994');
INSERT INTO public.ground_truth VALUES (235, 'libksba-1.6.5-3.el10', 3, 'Error: UNINIT (CWE-457):
libksba-1.6.5/src/der-builder.c:552: var_decl: Declaring variable "err" without initializer.
libksba-1.6.5/src/der-builder.c:669: uninit_use: Using uninitialized value "err".
#  667|    leave:
#  668|     xfree (buffer);
#  669|->   return err;
#  670|   }', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-34700

is fixed in latest release: https://dev.gnupg.org/T6992', 'Variable ''err'' is declared without an initializer (line 552) and may be used uninitialized at return (line 669) if specific error-setting code paths (e.g., lines 563, 580, 615) are not executed, as evident in identifiable vulnerable execution paths (e.g., when ''d'' is not NULL, ''d->error'' is not set, ''r_obj'' is not NULL, and ''d->finished'' is already true).', '2025-11-18 16:18:56.116994');
INSERT INTO public.ground_truth VALUES (236, 'libpcap-1.10.4-4.el10', 1, 'Error: UNINIT (CWE-457):
libpcap-1.10.4/bpf_filter.c:99:2: var_decl: Declaring variable "mem" without initializer.
libpcap-1.10.4/bpf_filter.c:222:4: uninit_use: Using uninitialized value "mem[pc->k]".
#  220|   
#  221|   		case BPF_LD|BPF_MEM:
#  222|-> 			A = mem[pc->k];
#  223|   			continue;
#  224|', true, 'Non-Issue', 'I tried to ask about this in the Upstream because I myself am unable to deduce this. The answer is that only if an invalid BPF program that does a load from a memory location without storing something there first is used as a filter. But loading an invalid bpf program should not be possible, that would end with an error. This is a key component of libpcap and it would have presented itself long time ago should this be a problem.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.12465');
INSERT INTO public.ground_truth VALUES (237, 'libpcap-1.10.4-4.el10', 2, 'Error: INTEGER_OVERFLOW (CWE-190):
libpcap-1.10.4/sf-pcap.c:682:4: underflow: The decrement operator on the unsigned variable "new_bufsize" might result in an underflow.
libpcap-1.10.4/sf-pcap.c:683:4: overflow: The expression "new_bufsize |= new_bufsize >> 1" is deemed underflowed because at least one of its arguments has underflowed.
libpcap-1.10.4/sf-pcap.c:684:4: overflow: The expression "new_bufsize |= new_bufsize >> 2" is deemed underflowed because at least one of its arguments has underflowed.
libpcap-1.10.4/sf-pcap.c:685:4: overflow: The expression "new_bufsize |= new_bufsize >> 4" is deemed underflowed because at least one of its arguments has underflowed.
libpcap-1.10.4/sf-pcap.c:686:4: overflow: The expression "new_bufsize |= new_bufsize >> 8" is deemed underflowed because at least one of its arguments has underflowed.
libpcap-1.10.4/sf-pcap.c:687:4: overflow: The expression "new_bufsize |= new_bufsize >> 16" is deemed underflowed because at least one of its arguments has underflowed.
libpcap-1.10.4/sf-pcap.c:693:4: overflow_sink: "new_bufsize", which might have underflowed, is passed to "grow_buffer(p, new_bufsize)".
#  691|   				new_bufsize = p->snapshot;
#  692|   
#  693|-> 			if (!grow_buffer(p, new_bufsize))
#  694|   				return (-1);
#  695|   		}', true, 'Non-Issue', 'This is a hack to actually get to the next power of two. The analysis is ignoring the new_bufsize++. Even if hdr->caplen is 0 (very unlikely) it will result in 1. p->bufsize is definitely not zero and thus the if condition above checks that hdr->caplen is at least bigger than that.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.12465');
INSERT INTO public.ground_truth VALUES (238, 'libpcap-1.10.4-4.el10', 3, 'Error: UNINIT (CWE-457):
libpcap-1.10.4/bpf_filter.c:99:2: var_decl: Declaring variable "mem" without initializer.
libpcap-1.10.4/bpf_filter.c:226:4: uninit_use: Using uninitialized value "mem[pc->k]".
#  224|   
#  225|   		case BPF_LDX|BPF_MEM:
#  226|-> 			X = mem[pc->k];
#  227|   			continue;
#  228|', true, 'Non-Issue', 'I tried to ask about this in the Upstream because I myself am unable to deduce this. The answer is that only if an invalid BPF program that does a load from a memory location without storing something there first is used as a filter. But loading an invalid bpf program should not be possible, that would end with an error. This is a key component of libpcap and it would have presented itself long time ago should this be a problem.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.12465');
INSERT INTO public.ground_truth VALUES (239, 'libpng-1.6.40-3.el10', 1, 'Error: OVERRUN (CWE-119):
libpng-1.6.40/pngwutil.c:529: assignment: Assigning: "avail_in" = "4294967295U".
libpng-1.6.40/pngwutil.c:536: assignment: Assigning: "png_ptr->zstream.avail_in" = "avail_in". The value of "png_ptr->zstream.avail_in" is now 4294967295.
libpng-1.6.40/pngwutil.c:580: overrun-buffer-arg: Calling "deflate" with "png_ptr->zstream.next_in" and "png_ptr->zstream.avail_in" is suspicious because of the very large index, 4294967295. The index may be due to a negative parameter being interpreted as unsigned.
#  578|   
#  579|            /* Compress the data */
#  580|->          ret = deflate(&png_ptr->zstream,
#  581|                input_len > 0 ? Z_NO_FLUSH : Z_FINISH);
#  582|', true, 'Non-Issue', NULL, 'Explicit assignment of a large value (4294967295U) to `png_ptr->zstream.avail_in` (lines 529, 536) is directly used in `deflate` (line 580) without explicit overflow checks, potentially triggering a buffer overrun due to signed/unsigned interpretation, with no clear mitigation in the provided code.', '2025-11-18 16:18:56.127843');
INSERT INTO public.ground_truth VALUES (240, 'libpng-1.6.40-3.el10', 2, 'Error: UNINIT (CWE-457):
libpng-1.6.40/pngrutil.c:1818: var_decl: Declaring variable "readbuf" without initializer.
libpng-1.6.40/pngrutil.c:1913: uninit_use_in_call: Using uninitialized value "*readbuf" when calling "png_set_tRNS".
# 1911|       * png_info.  Fix this.
# 1912|       */
# 1913|->    png_set_tRNS(png_ptr, info_ptr, readbuf, png_ptr->num_trans,
# 1914|          &(png_ptr->trans_color));
# 1915|   }', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-44993

', 'Uninitialized `readbuf` is used in `png_set_tRNS` (line 1913) if execution follows paths for `PNG_COLOR_TYPE_GRAY` or `PNG_COLOR_TYPE_RGB`, where `readbuf` remains uninitialized, directly correlating with the CVE''s description of using an uninitialized value.', '2025-11-18 16:18:56.127843');
INSERT INTO public.ground_truth VALUES (468, 'texinfo-7.1-2.el10', 23, 'Error: UNINIT (CWE-457):
texinfo-7.1/info/session.c:1121: var_decl: Declaring variable "iter" without initializer.
texinfo-7.1/info/session.c:1125: uninit_use: Using uninitialized value "iter.cur.wc_valid".
# 1123|   	    win->node->nodelen - point);
# 1124|     mbi_avail (iter);
# 1125|->   return mbi_cur (iter).wc_valid && mbi_cur (iter).wc == ''\n'';
# 1126|   }
# 1127|', true, 'Non-Issue', '"iter" is inicialized with mbi_init() on line 1122', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.203498');
INSERT INTO public.ground_truth VALUES (241, 'libpng-1.6.40-3.el10', 3, 'Error: INTEGER_OVERFLOW (CWE-190):
libpng-1.6.40/contrib/libtests/pngvalid.c:1314: underflow: The decrement operator on the unsigned variable "st" might result in an underflow.
libpng-1.6.40/contrib/libtests/pngvalid.c:1351: assign: Assigning: "cb" = "st".
libpng-1.6.40/contrib/libtests/pngvalid.c:1362: overflow: The expression "writepos += cb" is deemed underflowed because at least one of its arguments has underflowed.
libpng-1.6.40/contrib/libtests/pngvalid.c:1316: deref_overflow: "writepos++", which might have underflowed, is passed to "ps->new.buffer[writepos++]".
# 1314|            --st;
# 1315|            chunklen = (chunklen << 8) + b;
# 1316|->          ps->new.buffer[writepos++] = b;
# 1317|            ++chunkpos;
# 1318|         }', true, 'Non-Issue', 'st must be >0; see the while condition', 'Decrementing unsigned variable `st` at line 1314 wraps around to its maximum value due to unsigned integer arithmetic rules, mitigating the underflow concern, and subsequent reported issues at lines 1351, 1362, and 1316 are unfounded due to this context and the safe handling of `size_t` types within buffer bounds.', '2025-11-18 16:18:56.127843');
INSERT INTO public.ground_truth VALUES (242, 'libpng-1.6.40-3.el10', 4, 'Error: OVERRUN (CWE-119):
libpng-1.6.40/contrib/libtests/pngvalid.c:5055: identity_transfer: Passing field "pi->num_palette" (indirectly, via argument 2) to function "png_get_PLTE", which assigns it to "*npalette".
libpng-1.6.40/contrib/libtests/pngvalid.c:5059: cond_at_least: Checking "i <= 0" implies that "*npalette", "i" and "pi->num_palette" are at least 1 on the false branch.
libpng-1.6.40/contrib/libtests/pngvalid.c:5059: cond_between: Checking "i > 256" implies that "*npalette", "i" and "pi->num_palette" are between 1 and 256 (inclusive) on the false branch.
libpng-1.6.40/contrib/libtests/pngvalid.c:5072: overrun-local: Overrunning array of 1024 bytes at byte offset 1024 by dereferencing pointer "palette + *npalette". [Note: The source code implementation of the function has been overridden by a builtin model.]
# 5070|          * white/opaque which is the flag value stored above.)
# 5071|          */
# 5072|->       memset(palette + *npalette, 126, (256-*npalette) * sizeof *palette);
# 5073|      }
# 5074|', true, 'Non-Issue', 'if npalette us 256, then memset writes 0 bytes', 'Direct buffer overrun risk at line 5072 via `memset` with potentially out-of-bounds `palette + *npalette` offset, inadequately mitigated by prior bounds checks, introducing vulnerability under specific conditions.', '2025-11-18 16:18:56.127843');
INSERT INTO public.ground_truth VALUES (243, 'libpng-1.6.40-3.el10', 5, 'Error: OVERRUN (CWE-119):
libpng-1.6.40/pngrutil.c:2632: buffer_alloc: Calling allocating function "png_read_buffer" which allocates "length" bytes.
libpng-1.6.40/pngrutil.c:2632: var_assign: Assigning: "buffer" = "png_read_buffer(png_ptr, length, 2)".
libpng-1.6.40/pngrutil.c:2647: symbolic_compare: Tracking "keyword_length" since "length" is tracked with "buffer".
libpng-1.6.40/pngrutil.c:2659: symbolic_compare: Tracking "keyword_length + 3U" since "length" is tracked with "buffer".
libpng-1.6.40/pngrutil.c:2662: overrun-local: Overrunning dynamic array "buffer" at offset corresponding to index variable "keyword_length".
# 2660|         errmsg = "truncated";
# 2661|   
# 2662|->    else if (buffer[keyword_length+1] != PNG_COMPRESSION_TYPE_BASE)
# 2663|         errmsg = "unknown compression type";
# 2664|', true, 'Non-Issue', 'keyword_length is <= 79 and >=1 due to previous if', 'Explicit bounds checks at lines 2659 (keyword_length + 3 > length) and within the loop (lines 2647-2650, buffer[keyword_length] != 0) ensure ''keyword_length + 1'' at line 2662 is within the allocated ''buffer'' bounds, assuming ''png_read_buffer'' allocates correctly.', '2025-11-18 16:18:56.127843');
INSERT INTO public.ground_truth VALUES (244, 'libpng-1.6.40-3.el10', 6, 'Error: OVERRUN (CWE-119):
libpng-1.6.40/pngrutil.c:2632: buffer_alloc: Calling allocating function "png_read_buffer" which allocates "length" bytes.
libpng-1.6.40/pngrutil.c:2647: symbolic_compare: Tracking "keyword_length" since "length" is tracked with "png_ptr->read_buffer".
libpng-1.6.40/pngrutil.c:2659: symbolic_compare: Tracking "keyword_length + 3U" since "length" is tracked with "png_ptr->read_buffer".
libpng-1.6.40/pngrutil.c:2673: overrun-buffer-arg: Overrunning dynamic array "png_ptr->read_buffer" by passing it to a function that accesses it at byte "keyword_length + 2U - 1".
# 2671|          * and text chunks.
# 2672|          */
# 2673|->       if (png_decompress_chunk(png_ptr, length, keyword_length+2,
# 2674|             &uncompressed_length, 1/*terminate*/) == Z_STREAM_END)
# 2675|         {', true, 'Non-Issue', 'keyword_length is <= 79 and >=1 due to previous if', 'Potential buffer overrun at pngrutil.c:2673 due to accessing png_ptr->read_buffer at keyword_length + 2U - 1, with no explicit bounds check ensuring this offset doesn''t exceed the allocated length, directly correlating with the CVE''s buffer allocation and overrun concerns.', '2025-11-18 16:18:56.127843');
INSERT INTO public.ground_truth VALUES (245, 'libpng-1.6.40-3.el10', 7, 'Error: INTEGER_OVERFLOW (CWE-190):
libpng-1.6.40/contrib/tools/pngfix.c:2984: tainted_data_argument: The value returned in "file_crc" is considered tainted.
libpng-1.6.40/contrib/tools/pngfix.c:3068: underflow: The cast of "file_crc >> 24" to a signed type could result in a negative number.
# 3066|               ch = buffer[(++nused) & 7];
# 3067|   
# 3068|->          crc = crc_one_byte(crc, file_crc >> 24);
# 3069|            file_crc = (file_crc << 8) + ch;
# 3070|         }', true, 'Non-Issue', 'doesn''t matter, only lower 8 bits are used in crc_ony_byte()', 'Tainted data from `file_crc` (read at line 2984) is used without sanitization in a bitwise shift operation (line 3068) that casts to a signed type, potentially leading to underflow and undefined behavior, given the direct execution path within the `sync_stream` function.', '2025-11-18 16:18:56.127843');
INSERT INTO public.ground_truth VALUES (246, 'libpng-1.6.40-3.el10', 8, 'Error: OVERRUN (CWE-119):
libpng-1.6.40/png.c:1137: alias: Assigning: "errmsg" = ""duplicate"". "errmsg" now points to byte 0 of ""duplicate"" (which consists of 10 bytes).
libpng-1.6.40/png.c:1165: overrun-buffer-val: Overrunning buffer pointed to by "errmsg" of 10 bytes by passing it to a function which accesses it at byte offset 14.
# 1163|      /* Error exit - errmsg has been set. */
# 1164|      colorspace->flags |= PNG_COLORSPACE_INVALID;
# 1165|->    png_chunk_report(png_ptr, errmsg, PNG_CHUNK_WRITE_ERROR);
# 1166|   }
# 1167|', true, 'Non-Issue', 'it may overrun only if the message starts with "#", which is not this case', 'Unchanged default value indicates a failure in value replacement, confirming the issue''s validity.', '2025-11-18 16:18:56.127843');
INSERT INTO public.ground_truth VALUES (247, 'libpng-1.6.40-3.el10', 9, 'Error: OVERRUN (CWE-119):
libpng-1.6.40/pngwutil.c:973: assignment: Assigning: "avail" = "4294967295U".
libpng-1.6.40/pngwutil.c:978: assignment: Assigning: "png_ptr->zstream.avail_in" = "avail". The value of "png_ptr->zstream.avail_in" is now 4294967295.
libpng-1.6.40/pngwutil.c:981: overrun-buffer-arg: Calling "deflate" with "png_ptr->zstream.next_in" and "png_ptr->zstream.avail_in" is suspicious because of the very large index, 4294967295. The index may be due to a negative parameter being interpreted as unsigned.
#  979|         input_len -= avail;
#  980|   
#  981|->       ret = deflate(&png_ptr->zstream, input_len > 0 ? Z_NO_FLUSH : flush);
#  982|   
#  983|         /* Include as-yet unconsumed input */', true, 'Non-Issue', NULL, 'Assignment of `avail` to `4294967295U` (line 973) and subsequent use in `deflate` (line 981) poses a buffer overrun risk due to potential misinterpretation of a large unsigned value, with no explicit mitigations in the provided code snippet.', '2025-11-18 16:18:56.127843');
INSERT INTO public.ground_truth VALUES (248, 'libpng-1.6.40-3.el10', 10, 'Error: INTEGER_OVERFLOW (CWE-190):
libpng-1.6.40/contrib/libtests/pngvalid.c:1314: underflow: The decrement operator on the unsigned variable "st" might result in an underflow.
libpng-1.6.40/contrib/libtests/pngvalid.c:1351: assign: Assigning: "cb" = "st".
libpng-1.6.40/contrib/libtests/pngvalid.c:1359: overflow_sink: "cb", which might have underflowed, is passed to "memcpy(ps->new.buffer + writepos, pb, cb)". [Note: The source code implementation of the function has been overridden by a builtin model.]
# 1357|               cb = (size_t)/*SAFE*/(chunklen - chunkpos);
# 1358|   
# 1359|->          memcpy(ps->new.buffer + writepos, pb, cb);
# 1360|            chunkpos += (png_uint_32)/*SAFE*/cb;
# 1361|            pb += cb;', true, 'Non-Issue', 'st cannot underflow -> protected by the while condition', 'Decrementing unsigned `st` at line 1314 wraps to maximum value, not a traditional underflow vulnerability, and subsequent assignments/caps (lines 1353, 1356, 1357) for `cb` prevent overflow in `memcpy` at line 1359.', '2025-11-18 16:18:56.127843');
INSERT INTO public.ground_truth VALUES (249, 'libpng-1.6.40-3.el10', 11, 'Error: INTEGER_OVERFLOW (CWE-190):
libpng-1.6.40/contrib/tools/pngfix.c:3417: tainted_data_return: The value returned by "reread_byte(file)" is considered tainted.
libpng-1.6.40/contrib/tools/pngfix.c:3417: cast_overflow: An assign that casts to a different type, which might trigger an overflow.
libpng-1.6.40/contrib/tools/pngfix.c:3436: underflow: The cast of "b" to a signed type could result in a negative number.
# 3434|                     }
# 3435|   
# 3436|->                   chunk->write_crc = crc_one_byte(chunk->write_crc, b);
# 3437|                     break;
# 3438|', true, 'Non-Issue', 'doesn''t matter, only the lowest 8 bytes of "b" are used by crc_one_byte', 'Tainted data from `reread_byte(file)` is directly used in `crc_one_byte` without sanitization (line 3417), and subsequent casts between `png_byte` and `int` types (e.g., lines 3417, 3436) may trigger overflow or underflow, with no explicit mitigation in the provided code.', '2025-11-18 16:18:56.127843');
INSERT INTO public.ground_truth VALUES (250, 'libpng-1.6.40-3.el10', 12, 'Error: OVERRUN (CWE-119):
libpng-1.6.40/pngrutil.c:2746: buffer_alloc: Calling allocating function "png_read_buffer" which allocates "length + 1U + 1" bytes.
libpng-1.6.40/pngrutil.c:2746: var_assign: Assigning: "buffer" = "png_read_buffer(png_ptr, length + 1U, 1)".
libpng-1.6.40/pngrutil.c:2761: symbolic_compare: Tracking "prefix_length" since "length" is tracked with "buffer".
libpng-1.6.40/pngrutil.c:2774: symbolic_compare: Tracking "prefix_length + 5U" since "length" is tracked with "buffer".
libpng-1.6.40/pngrutil.c:2777: overrun-local: Overrunning dynamic array "buffer" at offset corresponding to index variable "prefix_length".
# 2775|         errmsg = "truncated";
# 2776|   
# 2777|->    else if (buffer[prefix_length+1] == 0 ||
# 2778|         (buffer[prefix_length+1] == 1 &&
# 2779|         buffer[prefix_length+2] == PNG_COMPRESSION_TYPE_BASE))', true, 'Non-Issue', 'keyword_length is <= 79 and >=1 due to previous if', 'Access to `buffer[prefix_length+1]` and `buffer[prefix_length+2]` at line 2777 may overrun the allocated buffer size (`length + 2` bytes) due to insufficient bounds checking, particularly since the `prefix_length + 5 > length` check at line 2774 does not directly prevent these accesses from exceeding the buffer bounds.', '2025-11-18 16:18:56.127843');
INSERT INTO public.ground_truth VALUES (251, 'libtalloc-2.4.2-1.el10', 1, 'Error: RESOURCE_LEAK (CWE-772):
talloc-2.4.2/testsuite.c:2155: alloc_fn: Storage is returned from allocation function "_talloc_realloc_array".
talloc-2.4.2/testsuite.c:2155: var_assign: Assigning: "p3" = storage returned from "_talloc_realloc_array(pool, p2, 4UL, 2048U, "int")".
talloc-2.4.2/testsuite.c:2193: leaked_storage: Variable "p3" going out of scope leaks the storage it points to.
# 2191|   
# 2192|   	printf("success: magic_free_protection\n");
# 2193|-> 	return true;
# 2194|   }
# 2195|', true, 'Non-Issue', 'This is the testsuite and Coverity doesn''t understand talloc without a modelling file.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.132952');
INSERT INTO public.ground_truth VALUES (252, 'libtalloc-2.4.2-1.el10', 2, 'Error: RESOURCE_LEAK (CWE-772):
talloc-2.4.2/testsuite.c:2155: alloc_fn: Storage is returned from allocation function "_talloc_realloc_array".
talloc-2.4.2/testsuite.c:2155: var_assign: Assigning: "p3" = storage returned from "_talloc_realloc_array(pool, p2, 4UL, 2048U, "int")".
talloc-2.4.2/testsuite.c:2179: leaked_storage: Variable "p3" going out of scope leaks the storage it points to.
# 2177|   	if (!WIFEXITED(exit_status)) {
# 2178|   		printf("Child exited through unexpected abnormal means\n");
# 2179|-> 		return false;
# 2180|   	}
# 2181|   	if (WEXITSTATUS(exit_status) != 42) {', true, 'Non-Issue', 'This is the testsuite and Coverity doesn''t understand talloc without a modelling file.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.132952');
INSERT INTO public.ground_truth VALUES (253, 'libtalloc-2.4.2-1.el10', 3, 'Error: RESOURCE_LEAK (CWE-772):
talloc-2.4.2/testsuite.c:1425: alloc_fn: Storage is returned from allocation function "_talloc_realloc".
talloc-2.4.2/testsuite.c:1425: var_assign: Assigning: "p2_2" = storage returned from "_talloc_realloc(root, p2, 128UL, "../../testsuite.c:1425")".
talloc-2.4.2/testsuite.c:1426: leaked_storage: Variable "p2_2" going out of scope leaks the storage it points to.
# 1424|   	/* now we should reclaim the full pool */
# 1425|   	p2_2 = talloc_realloc_size(root, p2, 8 * 16);
# 1426|-> 	torture_assert("pool realloc 8 * 16", p2_2 == p1, "failed: pointer not expected");
# 1427|   	p2 = p2_2;
# 1428|   	memset(p2_2, 0x11, talloc_get_size(p2_2));', true, 'Non-Issue', 'This is the testsuite and Coverity doesn''t understand talloc without a modelling file.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.132952');
INSERT INTO public.ground_truth VALUES (254, 'libtalloc-2.4.2-1.el10', 4, 'Error: RESOURCE_LEAK (CWE-772):
talloc-2.4.2/testsuite.c:1408: alloc_fn: Storage is returned from allocation function "_talloc_realloc".
talloc-2.4.2/testsuite.c:1408: var_assign: Assigning: "p1_2" = storage returned from "_talloc_realloc(root, p1, 80UL, "../../testsuite.c:1408")".
talloc-2.4.2/testsuite.c:1409: leaked_storage: Variable "p1_2" going out of scope leaks the storage it points to.
# 1407|   
# 1408|   	p1_2 = talloc_realloc_size(root, p1, 5 * 16);
# 1409|-> 	torture_assert("pool realloc 5 * 16", p1_2 > p2, "failed: pointer not changed");
# 1410|   	memset(p1_2, 0x11, talloc_get_size(p1_2));
# 1411|   	ofs1 = PTR_DIFF(p1_2, p2);', true, 'Non-Issue', 'This is the testsuite and Coverity doesn''t understand talloc without a modelling file.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.132952');
INSERT INTO public.ground_truth VALUES (255, 'libtalloc-2.4.2-1.el10', 5, 'Error: RESOURCE_LEAK (CWE-772):
talloc-2.4.2/testsuite.c:2155: alloc_fn: Storage is returned from allocation function "_talloc_realloc_array".
talloc-2.4.2/testsuite.c:2155: var_assign: Assigning: "p3" = storage returned from "_talloc_realloc_array(pool, p2, 4UL, 2048U, "int")".
talloc-2.4.2/testsuite.c:2183: leaked_storage: Variable "p3" going out of scope leaks the storage it points to.
# 2181|   	if (WEXITSTATUS(exit_status) != 42) {
# 2182|   		printf("Child exited with wrong exit status\n");
# 2183|-> 		return false;
# 2184|   	}
# 2185|   	if (WIFSIGNALED(exit_status)) {', true, 'Non-Issue', 'This is the testsuite and Coverity doesn''t understand talloc without a modelling file.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.132952');
INSERT INTO public.ground_truth VALUES (256, 'libtalloc-2.4.2-1.el10', 6, 'Error: RESOURCE_LEAK (CWE-772):
talloc-2.4.2/testsuite.c:1826: alloc_fn: Storage is returned from allocation function "_talloc_realloc".
talloc-2.4.2/testsuite.c:1826: var_assign: Assigning: "l2" = storage returned from "_talloc_realloc(NULL, l1, 10240UL, "../../testsuite.c:1826")".
talloc-2.4.2/testsuite.c:1827: leaked_storage: Variable "l2" going out of scope leaks the storage it points to.
# 1825|   	printf("==== talloc_realloc_size(NULL, l1, 10*1024) 10/10\n");
# 1826|   	l2 = talloc_realloc_size(NULL, l1, 10*1024);
# 1827|-> 	torture_assert("memlimit", l2 == NULL,
# 1828|   			"failed: realloc should fail due to memory limit\n");
# 1829|', true, 'Non-Issue', 'This is the testsuite and Coverity doesn''t understand talloc without a modelling file.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.132952');
INSERT INTO public.ground_truth VALUES (257, 'libtalloc-2.4.2-1.el10', 7, 'Error: RESOURCE_LEAK (CWE-772):
talloc-2.4.2/testsuite.c:1323: alloc_fn: Storage is returned from allocation function "_talloc_realloc".
talloc-2.4.2/testsuite.c:1323: var_assign: Assigning: "p2_2" = storage returned from "_talloc_realloc(pool, p2, 400UL, "../../testsuite.c:1323")".
talloc-2.4.2/testsuite.c:1324: leaked_storage: Variable "p2_2" going out of scope leaks the storage it points to.
# 1322|   	/* this should reclaim the memory of p4 and p3 */
# 1323|   	p2_2 = talloc_realloc_size(pool, p2, 400);
# 1324|-> 	torture_assert("pool realloc 400", p2_2 == p2, "failed: pointer changed");
# 1325|   	memset(p2, 0x11, talloc_get_size(p2));
# 1326|', true, 'Non-Issue', 'This is the testsuite and Coverity doesn''t understand talloc without a modelling file.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.132952');
INSERT INTO public.ground_truth VALUES (258, 'libtalloc-2.4.2-1.el10', 8, 'Error: RESOURCE_LEAK (CWE-772):
talloc-2.4.2/testsuite.c:1853: alloc_fn: Storage is returned from allocation function "_talloc_realloc".
talloc-2.4.2/testsuite.c:1853: var_assign: Assigning: "l2" = storage returned from "_talloc_realloc(NULL, l1, 10240UL, "../../testsuite.c:1853")".
talloc-2.4.2/testsuite.c:1854: leaked_storage: Variable "l2" going out of scope leaks the storage it points to.
# 1852|   	printf("==== talloc_realloc_size(NULL, l1, 10*1024)\n");
# 1853|   	l2 = talloc_realloc_size(NULL, l1, 10*1024);
# 1854|-> 	torture_assert("memlimit", l2 == NULL,
# 1855|   			"failed: realloc should fail due to memory limit\n");
# 1856|', true, 'Non-Issue', 'This is the testsuite and Coverity doesn''t understand talloc without a modelling file.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.132952');
INSERT INTO public.ground_truth VALUES (259, 'libtalloc-2.4.2-1.el10', 9, 'Error: RESOURCE_LEAK (CWE-772):
talloc-2.4.2/testsuite.c:1356: alloc_fn: Storage is returned from allocation function "_talloc_realloc".
talloc-2.4.2/testsuite.c:1356: var_assign: Assigning: "p2_2" = storage returned from "_talloc_realloc(pool, p2, 19UL, "../../testsuite.c:1356")".
talloc-2.4.2/testsuite.c:1357: leaked_storage: Variable "p2_2" going out of scope leaks the storage it points to.
# 1355|   
# 1356|   	p2_2 = talloc_realloc_size(pool, p2, 20-1);
# 1357|-> 	torture_assert("pool realloc 20-1", p2_2 == p2, "failed: pointer changed");
# 1358|   	memset(p2, 0x11, talloc_get_size(p2));
# 1359|   	p2_2 = talloc_realloc_size(pool, p2, 20-1);', true, 'Non-Issue', 'This is the testsuite and Coverity doesn''t understand talloc without a modelling file.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.132952');
INSERT INTO public.ground_truth VALUES (260, 'libtalloc-2.4.2-1.el10', 10, 'Error: RESOURCE_LEAK (CWE-772):
talloc-2.4.2/testsuite.c:1408: alloc_fn: Storage is returned from allocation function "_talloc_realloc".
talloc-2.4.2/testsuite.c:1408: var_assign: Assigning: "p1_2" = storage returned from "_talloc_realloc(root, p1, 80UL, "../../testsuite.c:1408")".
talloc-2.4.2/testsuite.c:1410: noescape: Resource "p1_2" is not freed or pointed-to in "talloc_get_size".
talloc-2.4.2/testsuite.c:1410: noescape: Resource "p1_2" is not freed or pointed-to in "memset". [Note: The source code implementation of the function has been overridden by a builtin model.]
talloc-2.4.2/testsuite.c:1414: leaked_storage: Variable "p1_2" going out of scope leaks the storage it points to.
# 1412|   	ofs2 = talloc_get_size(p2) + hdr;
# 1413|   
# 1414|-> 	torture_assert("pool realloc ", ofs1 == ofs2, "failed: pointer offset unexpected");
# 1415|   
# 1416|   	p2_2 = talloc_realloc_size(root, p2, 3 * 16);', true, 'Non-Issue', 'This is the testsuite and Coverity doesn''t understand talloc without a modelling file.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.132952');
INSERT INTO public.ground_truth VALUES (261, 'libtalloc-2.4.2-1.el10', 11, 'Error: RESOURCE_LEAK (CWE-772):
talloc-2.4.2/testsuite.c:1359: alloc_fn: Storage is returned from allocation function "_talloc_realloc".
talloc-2.4.2/testsuite.c:1359: var_assign: Assigning: "p2_2" = storage returned from "_talloc_realloc(pool, p2, 19UL, "../../testsuite.c:1359")".
talloc-2.4.2/testsuite.c:1360: leaked_storage: Variable "p2_2" going out of scope leaks the storage it points to.
# 1358|   	memset(p2, 0x11, talloc_get_size(p2));
# 1359|   	p2_2 = talloc_realloc_size(pool, p2, 20-1);
# 1360|-> 	torture_assert("pool realloc 20-1", p2_2 == p2, "failed: pointer changed");
# 1361|   	memset(p2, 0x11, talloc_get_size(p2));
# 1362|', true, 'Non-Issue', 'This is the testsuite and Coverity doesn''t understand talloc without a modelling file.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.132952');
INSERT INTO public.ground_truth VALUES (262, 'libtalloc-2.4.2-1.el10', 12, 'Error: RESOURCE_LEAK (CWE-772):
talloc-2.4.2/testsuite.c:1310: alloc_fn: Storage is returned from allocation function "_talloc_realloc".
talloc-2.4.2/testsuite.c:1310: var_assign: Assigning: "p2_2" = storage returned from "_talloc_realloc(pool, p2, 21UL, "../../testsuite.c:1310")".
talloc-2.4.2/testsuite.c:1311: leaked_storage: Variable "p2_2" going out of scope leaks the storage it points to.
# 1309|   
# 1310|   	p2_2 = talloc_realloc_size(pool, p2, 20+1);
# 1311|-> 	torture_assert("pool realloc 20+1", p2_2 == p2, "failed: pointer changed");
# 1312|   	memset(p2, 0x11, talloc_get_size(p2));
# 1313|   	p2_2 = talloc_realloc_size(pool, p2, 20-1);', true, 'Non-Issue', 'This is the testsuite and Coverity doesn''t understand talloc without a modelling file.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.132952');
INSERT INTO public.ground_truth VALUES (263, 'libtalloc-2.4.2-1.el10', 13, 'Error: RESOURCE_LEAK (CWE-772):
talloc-2.4.2/testsuite.c:1815: alloc_fn: Storage is returned from allocation function "_talloc_realloc".
talloc-2.4.2/testsuite.c:1815: var_assign: Assigning: "l2" = storage returned from "_talloc_realloc(NULL, l1, 10240UL, "../../testsuite.c:1815")".
talloc-2.4.2/testsuite.c:1816: leaked_storage: Variable "l2" going out of scope leaks the storage it points to.
# 1814|   	printf("==== talloc_realloc_size(NULL, l1, 10*1024) 10/10\n");
# 1815|   	l2 = talloc_realloc_size(NULL, l1, 10*1024);
# 1816|-> 	torture_assert("memlimit", l2 == NULL,
# 1817|   			"failed: realloc should fail due to memory limit\n");
# 1818|', true, 'Non-Issue', 'This is the testsuite and Coverity doesn''t understand talloc without a modelling file.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.132952');
INSERT INTO public.ground_truth VALUES (264, 'libtalloc-2.4.2-1.el10', 14, 'Error: RESOURCE_LEAK (CWE-772):
talloc-2.4.2/testsuite.c:1364: alloc_fn: Storage is returned from allocation function "_talloc_realloc".
talloc-2.4.2/testsuite.c:1364: var_assign: Assigning: "p2_2" = storage returned from "_talloc_realloc(pool, p2, 1800UL, "../../testsuite.c:1364")".
talloc-2.4.2/testsuite.c:1366: var_assign: Assigning: "p2" = "p2_2".
talloc-2.4.2/testsuite.c:1367: noescape: Resource "p2" is not freed or pointed-to in "talloc_get_size".
talloc-2.4.2/testsuite.c:1367: noescape: Resource "p2" is not freed or pointed-to in "memset". [Note: The source code implementation of the function has been overridden by a builtin model.]
talloc-2.4.2/testsuite.c:1376: leaked_storage: Variable "p2_2" going out of scope leaks the storage it points to.
talloc-2.4.2/testsuite.c:1376: leaked_storage: Variable "p2" going out of scope leaks the storage it points to.
# 1374|   	talloc_free(pool);
# 1375|   
# 1376|-> 	return true;
# 1377|   }
# 1378|', true, 'Non-Issue', 'This is the testsuite and Coverity doesn''t understand talloc without a modelling file.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.132952');
INSERT INTO public.ground_truth VALUES (265, 'libtalloc-2.4.2-1.el10', 15, 'Error: RESOURCE_LEAK (CWE-772):
talloc-2.4.2/testsuite.c:1313: alloc_fn: Storage is returned from allocation function "_talloc_realloc".
talloc-2.4.2/testsuite.c:1313: var_assign: Assigning: "p2_2" = storage returned from "_talloc_realloc(pool, p2, 19UL, "../../testsuite.c:1313")".
talloc-2.4.2/testsuite.c:1314: leaked_storage: Variable "p2_2" going out of scope leaks the storage it points to.
# 1312|   	memset(p2, 0x11, talloc_get_size(p2));
# 1313|   	p2_2 = talloc_realloc_size(pool, p2, 20-1);
# 1314|-> 	torture_assert("pool realloc 20-1", p2_2 == p2, "failed: pointer changed");
# 1315|   	memset(p2, 0x11, talloc_get_size(p2));
# 1316|   	p2_2 = talloc_realloc_size(pool, p2, 20-1);', true, 'Non-Issue', 'This is the testsuite and Coverity doesn''t understand talloc without a modelling file.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.132952');
INSERT INTO public.ground_truth VALUES (266, 'libtalloc-2.4.2-1.el10', 16, 'Error: RESOURCE_LEAK (CWE-772):
talloc-2.4.2/testsuite.c:1019: alloc_fn: Storage is returned from allocation function "_talloc_realloc_array".
talloc-2.4.2/testsuite.c:1019: var_assign: Assigning: "a" = storage returned from "_talloc_realloc_array(parent, a, 1UL, 2048U, "char")".
talloc-2.4.2/testsuite.c:1032: leaked_storage: Variable "a" going out of scope leaks the storage it points to.
# 1030|   	talloc_free(parent);
# 1031|   
# 1032|-> 	torture_assert("check destructor realloc_parent_destructor",
# 1033|   		       realloc_parent_destructor_count == 2,
# 1034|   		       "FAILED TO FIRE free_for_exit_destructor\n");', true, 'Non-Issue', 'This is the testsuite and Coverity doesn''t understand talloc without a modelling file.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.132952');
INSERT INTO public.ground_truth VALUES (267, 'libtalloc-2.4.2-1.el10', 17, 'Error: RESOURCE_LEAK (CWE-772):
talloc-2.4.2/testsuite.c:580: alloc_fn: Storage is returned from allocation function "_talloc_realloc".
talloc-2.4.2/testsuite.c:580: leaked_storage: Failing to save or free storage allocated by "_talloc_realloc(NULL, p2, 5UL, "../../testsuite.c:580")" leaks it.
#  578|   
#  579|   	talloc_increase_ref_count(p2);
#  580|-> 	torture_assert("realloc", talloc_realloc_size(NULL, p2, 5) == NULL,
#  581|   		"failed: talloc_realloc() on a referenced pointer should fail\n");
#  582|   	CHECK_BLOCKS("realloc", p1, 4);', true, 'Non-Issue', 'This is the testsuite and Coverity doesn''t understand talloc without a modelling file.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.132952');
INSERT INTO public.ground_truth VALUES (268, 'libtalloc-2.4.2-1.el10', 18, 'Error: RESOURCE_LEAK (CWE-772):
talloc-2.4.2/testsuite.c:1408: alloc_fn: Storage is returned from allocation function "_talloc_realloc".
talloc-2.4.2/testsuite.c:1408: var_assign: Assigning: "p1_2" = storage returned from "_talloc_realloc(root, p1, 80UL, "../../testsuite.c:1408")".
talloc-2.4.2/testsuite.c:1410: noescape: Resource "p1_2" is not freed or pointed-to in "talloc_get_size".
talloc-2.4.2/testsuite.c:1410: noescape: Resource "p1_2" is not freed or pointed-to in "memset". [Note: The source code implementation of the function has been overridden by a builtin model.]
talloc-2.4.2/testsuite.c:1417: leaked_storage: Variable "p1_2" going out of scope leaks the storage it points to.
# 1415|   
# 1416|   	p2_2 = talloc_realloc_size(root, p2, 3 * 16);
# 1417|-> 	torture_assert("pool realloc 5 * 16", p2_2 == p2, "failed: pointer changed");
# 1418|   	memset(p2_2, 0x11, talloc_get_size(p2_2));
# 1419|', true, 'Non-Issue', 'This is the testsuite and Coverity doesn''t understand talloc without a modelling file.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.132952');
INSERT INTO public.ground_truth VALUES (269, 'libtalloc-2.4.2-1.el10', 19, 'Error: RESOURCE_LEAK (CWE-772):
talloc-2.4.2/testsuite.c:810: alloc_fn: Storage is returned from allocation function "talloc_realloc_fn".
talloc-2.4.2/testsuite.c:810: var_assign: Assigning: "p1" = storage returned from "talloc_realloc_fn(root, p1, 20UL)".
talloc-2.4.2/testsuite.c:811: leaked_storage: Variable "p1" going out of scope leaks the storage it points to.
#  809|   	CHECK_SIZE("realloc_fn", root, 10);
#  810|   	p1 = talloc_realloc_fn(root, p1, 20);
#  811|-> 	CHECK_BLOCKS("realloc_fn", root, 2);
#  812|   	CHECK_SIZE("realloc_fn", root, 20);
#  813|   	p1 = talloc_realloc_fn(root, p1, 0);', true, 'Non-Issue', 'This is the testsuite and Coverity doesn''t understand talloc without a modelling file.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.132952');
INSERT INTO public.ground_truth VALUES (270, 'libtalloc-2.4.2-1.el10', 20, 'Error: RESOURCE_LEAK (CWE-772):
talloc-2.4.2/testsuite.c:1336: alloc_fn: Storage is returned from allocation function "_talloc_realloc".
talloc-2.4.2/testsuite.c:1336: var_assign: Assigning: "p2_2" = storage returned from "_talloc_realloc(pool, p2, 1800UL, "../../testsuite.c:1336")".
talloc-2.4.2/testsuite.c:1338: var_assign: Assigning: "p2" = "p2_2".
talloc-2.4.2/testsuite.c:1339: noescape: Resource "p2" is not freed or pointed-to in "talloc_get_size".
talloc-2.4.2/testsuite.c:1339: noescape: Resource "p2" is not freed or pointed-to in "memset". [Note: The source code implementation of the function has been overridden by a builtin model.]
talloc-2.4.2/testsuite.c:1343: leaked_storage: Variable "p2_2" going out of scope leaks the storage it points to.
talloc-2.4.2/testsuite.c:1343: leaked_storage: Variable "p2" going out of scope leaks the storage it points to.
# 1341|   	/* this should reclaim the memory from the pool */
# 1342|   	p3 = talloc_size(pool, 80);
# 1343|-> 	torture_assert("pool alloc 80", p3 == p1, "failed: pointer changed");
# 1344|   	memset(p3, 0x11, talloc_get_size(p3));
# 1345|', true, 'Non-Issue', 'This is the testsuite and Coverity doesn''t understand talloc without a modelling file.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.132952');
INSERT INTO public.ground_truth VALUES (432, 'sqlite-3.45.1-2.el10', 101, 'Error: UNINIT (CWE-457):
sqlite-src-3450100/sqlite3_analyzer.c:204869: skipped_decl: Jumping over declaration of "opcode".
sqlite-src-3450100/sqlite3_analyzer.c:204899: uninit_use: Using uninitialized value "opcode".
#204897|              || c==''n'' || c==''r'' || c==''t''
#204898|              || (c==''u'' && jsonIs4Hex(&z[j+1])) ){
#204899|->           if( opcode==JSONB_TEXT ) opcode = JSONB_TEXTJ;
#204900|           }else if( c==''\'''' || c==''0'' || c==''v'' || c==''\n''
#204901|              || (0xe2==(u8)c && 0x80==(u8)z[j+1]', true, 'Non-Issue', 'opcode is set ahead of jumping to parse_string flag.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (271, 'libtalloc-2.4.2-1.el10', 21, 'Error: RESOURCE_LEAK (CWE-772):
talloc-2.4.2/testsuite.c:1721: alloc_fn: Storage is returned from allocation function "_talloc_realloc_array".
talloc-2.4.2/testsuite.c:1721: var_assign: Assigning: "l5" = storage returned from "_talloc_realloc_array(NULL, l5, 1UL, 5U, "char")".
talloc-2.4.2/testsuite.c:1741: overwrite_var: Overwriting "l5" in "l5 = talloc_strdup(l4, "level 5")" leaks the storage that "l5" points to.
# 1739|   
# 1740|   	printf("==== talloc_strdup(l4, level 5)\n");
# 1741|-> 	l5 = talloc_strdup(l4, "level 5");
# 1742|   	torture_assert("memlimit", l5 != NULL,
# 1743|   		"failed: alloc should not fail due to memory limit\n");', true, 'Non-Issue', 'This is the testsuite and Coverity doesn''t understand talloc without a modelling file.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.132952');
INSERT INTO public.ground_truth VALUES (272, 'libtalloc-2.4.2-1.el10', 22, 'Error: RESOURCE_LEAK (CWE-772):
talloc-2.4.2/testsuite.c:1714: alloc_fn: Storage is returned from allocation function "_talloc_realloc_array".
talloc-2.4.2/testsuite.c:1714: var_assign: Assigning: "t" = storage returned from "_talloc_realloc_array(NULL, l5, 1UL, 600U, "char")".
talloc-2.4.2/testsuite.c:1715: leaked_storage: Variable "t" going out of scope leaks the storage it points to.
# 1713|   	printf("==== talloc_realloc(NULL, l5, char, 600)\n");
# 1714|   	t = talloc_realloc(NULL, l5, char, 600);
# 1715|-> 	torture_assert("memlimit", t == NULL,
# 1716|   		"failed: alloc should fail due to memory limit\n");
# 1717|', true, 'Non-Issue', 'This is the testsuite and Coverity doesn''t understand talloc without a modelling file.', 'Assignment to ''t'' is within a testsuite, explicitly expecting allocation failure due to memory limits (line 1715), and does not indicate a genuine memory leak scenario, aligning with known false positive patterns in test contexts without talloc modeling.', '2025-11-18 16:18:56.132952');
INSERT INTO public.ground_truth VALUES (273, 'libtalloc-2.4.2-1.el10', 23, 'Error: RESOURCE_LEAK (CWE-772):
talloc-2.4.2/testsuite.c:2155: alloc_fn: Storage is returned from allocation function "_talloc_realloc_array".
talloc-2.4.2/testsuite.c:2155: var_assign: Assigning: "p3" = storage returned from "_talloc_realloc_array(pool, p2, 4UL, 2048U, "int")".
talloc-2.4.2/testsuite.c:2172: leaked_storage: Variable "p3" going out of scope leaks the storage it points to.
# 2170|   
# 2171|   		/* Never reached. Make compilers happy */
# 2172|-> 		return true;
# 2173|   	}
# 2174|', true, 'Non-Issue', 'This is the testsuite and Coverity doesn''t understand talloc without a modelling file.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.132952');
INSERT INTO public.ground_truth VALUES (274, 'libtalloc-2.4.2-1.el10', 24, 'Error: RESOURCE_LEAK (CWE-772):
talloc-2.4.2/testsuite.c:1364: alloc_fn: Storage is returned from allocation function "_talloc_realloc".
talloc-2.4.2/testsuite.c:1364: var_assign: Assigning: "p2_2" = storage returned from "_talloc_realloc(pool, p2, 1800UL, "../../testsuite.c:1364")".
talloc-2.4.2/testsuite.c:1366: var_assign: Assigning: "p2" = "p2_2".
talloc-2.4.2/testsuite.c:1367: noescape: Resource "p2" is not freed or pointed-to in "talloc_get_size".
talloc-2.4.2/testsuite.c:1367: noescape: Resource "p2" is not freed or pointed-to in "memset". [Note: The source code implementation of the function has been overridden by a builtin model.]
talloc-2.4.2/testsuite.c:1371: leaked_storage: Variable "p2_2" going out of scope leaks the storage it points to.
talloc-2.4.2/testsuite.c:1371: leaked_storage: Variable "p2" going out of scope leaks the storage it points to.
# 1369|   	/* this should reclaim the memory from the pool */
# 1370|   	p3 = talloc_size(pool, 800);
# 1371|-> 	torture_assert("pool alloc 800", p3 == p1, "failed: pointer changed");
# 1372|   	memset(p3, 0x11, talloc_get_size(p3));
# 1373|', true, 'Non-Issue', 'This is the testsuite and Coverity doesn''t understand talloc without a modelling file.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.132952');
INSERT INTO public.ground_truth VALUES (275, 'libtalloc-2.4.2-1.el10', 25, 'Error: RESOURCE_LEAK (CWE-772):
talloc-2.4.2/testsuite.c:1316: alloc_fn: Storage is returned from allocation function "_talloc_realloc".
talloc-2.4.2/testsuite.c:1316: var_assign: Assigning: "p2_2" = storage returned from "_talloc_realloc(pool, p2, 19UL, "../../testsuite.c:1316")".
talloc-2.4.2/testsuite.c:1317: leaked_storage: Variable "p2_2" going out of scope leaks the storage it points to.
# 1315|   	memset(p2, 0x11, talloc_get_size(p2));
# 1316|   	p2_2 = talloc_realloc_size(pool, p2, 20-1);
# 1317|-> 	torture_assert("pool realloc 20-1", p2_2 == p2, "failed: pointer changed");
# 1318|   	memset(p2, 0x11, talloc_get_size(p2));
# 1319|', true, 'Non-Issue', 'This is the testsuite and Coverity doesn''t understand talloc without a modelling file.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.132952');
INSERT INTO public.ground_truth VALUES (276, 'libtalloc-2.4.2-1.el10', 26, 'Error: COPY_PASTE_ERROR (CWE-398):
talloc-2.4.2/talloc.c:2385: original: "tc2->next" looks like the original copy.
talloc-2.4.2/talloc.c:2381: copy_paste_error: "next" in "tc2->next" looks like a copy-paste error.
talloc-2.4.2/talloc.c:2381: remediation: Should it say "child" instead?
# 2379|   		struct talloc_chunk *tc, *tc2;
# 2380|   		tc = talloc_chunk_from_ptr(null_context);
# 2381|-> 		for (tc2 = tc->child; tc2; tc2=tc2->next) {
# 2382|   			if (tc2->parent == tc) tc2->parent = NULL;
# 2383|   			if (tc2->prev == tc) tc2->prev = NULL;', true, 'Non-Issue', 'This is the testsuite and Coverity doesn''t understand talloc without a modelling file.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.132952');
INSERT INTO public.ground_truth VALUES (277, 'libtalloc-2.4.2-1.el10', 27, 'Error: RESOURCE_LEAK (CWE-772):
talloc-2.4.2/testsuite.c:1416: alloc_fn: Storage is returned from allocation function "_talloc_realloc".
talloc-2.4.2/testsuite.c:1416: var_assign: Assigning: "p2_2" = storage returned from "_talloc_realloc(root, p2, 48UL, "../../testsuite.c:1416")".
talloc-2.4.2/testsuite.c:1417: leaked_storage: Variable "p2_2" going out of scope leaks the storage it points to.
# 1415|   
# 1416|   	p2_2 = talloc_realloc_size(root, p2, 3 * 16);
# 1417|-> 	torture_assert("pool realloc 5 * 16", p2_2 == p2, "failed: pointer changed");
# 1418|   	memset(p2_2, 0x11, talloc_get_size(p2_2));
# 1419|', true, 'Non-Issue', 'This is the testsuite and Coverity doesn''t understand talloc without a modelling file.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.132952');
INSERT INTO public.ground_truth VALUES (278, 'libtalloc-2.4.2-1.el10', 28, 'Error: RESOURCE_LEAK (CWE-772):
talloc-2.4.2/testsuite.c:1330: alloc_fn: Storage is returned from allocation function "_talloc_realloc".
talloc-2.4.2/testsuite.c:1330: var_assign: Assigning: "p2_2" = storage returned from "_talloc_realloc(pool, p2, 800UL, "../../testsuite.c:1330")".
talloc-2.4.2/testsuite.c:1331: leaked_storage: Variable "p2_2" going out of scope leaks the storage it points to.
# 1329|   	/* this should reclaim the memory of p1 */
# 1330|   	p2_2 = talloc_realloc_size(pool, p2, 800);
# 1331|-> 	torture_assert("pool realloc 800", p2_2 == p1, "failed: pointer not changed");
# 1332|   	p2 = p2_2;
# 1333|   	memset(p2, 0x11, talloc_get_size(p2));', true, 'Non-Issue', 'This is the testsuite and Coverity doesn''t understand talloc without a modelling file.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.132952');
INSERT INTO public.ground_truth VALUES (279, 'libtalloc-2.4.2-1.el10', 29, 'Error: RESOURCE_LEAK (CWE-772):
talloc-2.4.2/testsuite.c:1837: alloc_fn: Storage is returned from allocation function "_talloc_realloc".
talloc-2.4.2/testsuite.c:1837: var_assign: Assigning: "l2" = storage returned from "_talloc_realloc(NULL, l1, 10240UL, "../../testsuite.c:1837")".
talloc-2.4.2/testsuite.c:1853: overwrite_var: Overwriting "l2" in "l2 = _talloc_realloc(NULL, l1, 10240UL, "../../testsuite.c:1853")" leaks the storage that "l2" points to.
# 1851|   	/* But reallocs bigger than the pool will still fail */
# 1852|   	printf("==== talloc_realloc_size(NULL, l1, 10*1024)\n");
# 1853|-> 	l2 = talloc_realloc_size(NULL, l1, 10*1024);
# 1854|   	torture_assert("memlimit", l2 == NULL,
# 1855|   			"failed: realloc should fail due to memory limit\n");', true, 'Non-Issue', 'This is the testsuite and Coverity doesn''t understand talloc without a modelling file.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.132952');
INSERT INTO public.ground_truth VALUES (280, 'libuser-0.64-7.el10', 1, 'Error: INTEGER_OVERFLOW (CWE-190):
libuser-0.64/lib/config.c:152: tainted_data_return: Called function "read(fd, dest, left)", and a possible return value may be less than zero.
libuser-0.64/lib/config.c:152: assign: Assigning: "res" = "read(fd, dest, left)".
libuser-0.64/lib/config.c:164: overflow: The expression "left -= res" might be negative, but is used in a context that treats it as unsigned.
libuser-0.64/lib/config.c:152: overflow_sink: "left", which might be negative, is passed to "read(fd, dest, left)". [Note: The source code implementation of the function has been overridden by a builtin model.]
#  150|   		ssize_t res;
#  151|   
#  152|-> 		res = read(fd, dest, left);
#  153|   		if (res == 0)
#  154|   			break;', true, 'Non-Issue', 'Only negative number that can be returned from read() is -1 and that is explicetly handled.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.14124');
INSERT INTO public.ground_truth VALUES (281, 'libuser-0.64-7.el10', 2, 'Error: UNINIT (CWE-457):
libuser-0.64/lib/getdate.y:786: var_decl: Declaring variable "tm" without initializer.
libuser-0.64/lib/getdate.y:835: uninit_use: Using uninitialized value "tm". Field "tm.tm_wday" is uninitialized.
#  833|     tm.tm_sec += yy.RelSeconds;
#  834|     tm.tm_isdst = -1;
#  835|->   tm0 = tm;
#  836|   
#  837|     Start = mktime (&tm);', true, 'Non-Issue', '`tm0` is used just as backup and in partcular case (Start == -1 and yy.HaveZone) tm is assigned back to tm0 (tm = tm0). But after that mktime() is called a few lines later so the wday is set. ', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.14124');
INSERT INTO public.ground_truth VALUES (282, 'libuser-0.64-7.el10', 3, 'Error: RESOURCE_LEAK (CWE-772):
libuser-0.64/lib/config.c:480: alloc_fn: Storage is returned from allocation function "lu_cfg_read".
libuser-0.64/lib/config.c:480: var_assign: Assigning: "answers" = storage returned from "lu_cfg_read(context, key, NULL)".
libuser-0.64/lib/config.c:488: leaked_storage: Variable "answers" going out of scope leaks the storage it points to.
#  486|   		ret = context->scache->cache(context->scache, default_value);
#  487|   
#  488|-> 	return ret;
#  489|   }
#  490|', true, 'Non-Issue', 'In our case checking answers is redundant. It can never happed that `answers` is not null and `answers->data` is. So the `answers` never leaks.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.14124');
INSERT INTO public.ground_truth VALUES (283, 'libuser-0.64-7.el10', 4, 'Error: OVERRUN (CWE-119):
libuser-0.64/lib/getdate.c:1081: assignment: Assigning: "yystacksize" = "200L".
libuser-0.64/lib/getdate.c:1168: assignment: Assigning: "yystacksize" *= "2L". The value of "yystacksize" is now 400.
libuser-0.64/lib/getdate.c:1179: alias: Assigning: "yyss" = "&yyptr->yyss_alloc". "yyss" now points to byte 0 of "yyptr->yyss_alloc" (which consists of 4 bytes).
libuser-0.64/lib/getdate.c:1195: illegal_address: "yyss + yystacksize - 1" evaluates to an address that is at byte offset 399 of an array of 4 bytes.
# 1193|         YY_IGNORE_USELESS_CAST_END
# 1194|   
# 1195|->       if (yyss + yystacksize - 1 <= yyssp)
# 1196|           YYABORT;
# 1197|       }', true, 'Non-Issue', 'Bison generated code. LGTM', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.14124');
INSERT INTO public.ground_truth VALUES (284, 'libuser-0.64-7.el10', 5, 'Error: INTEGER_OVERFLOW (CWE-190):
libuser-0.64/lib/fs.c:239: tainted_data_return: Called function "write(dest_fd, p, left)", and a possible return value may be less than zero.
libuser-0.64/lib/fs.c:239: assign: Assigning: "out" = "write(dest_fd, p, left)".
libuser-0.64/lib/fs.c:249: overflow: The expression "left" is considered to have possibly overflowed.
libuser-0.64/lib/fs.c:239: overflow_sink: "left", which might have overflowed, is passed to "write(dest_fd, p, left)".
#  237|   			ssize_t out;
#  238|   
#  239|-> 			out = write(dest_fd, p, left);
#  240|   			if (out == -1) {
#  241|   				if (errno == EINTR)', true, 'Non-Issue', 'Only negative number that can be returned from write() is -1 and that is explicetly handled.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.14124');
INSERT INTO public.ground_truth VALUES (285, 'libuser-0.64-7.el10', 6, 'Error: UNINIT (CWE-457):
libuser-0.64/lib/getdate.c:1089: var_decl: Declaring variable "yyvsa" without initializer.
libuser-0.64/lib/getdate.c:1090: assign: Assigning: "yyvs" = "yyvsa", which points to uninitialized data.
libuser-0.64/lib/getdate.c:1180: uninit_use_in_call: Using uninitialized value "*yyvs" when calling "__builtin_memcpy".
# 1178|             YYNOMEM;
# 1179|           YYSTACK_RELOCATE (yyss_alloc, yyss);
# 1180|->         YYSTACK_RELOCATE (yyvs_alloc, yyvs);
# 1181|   #  undef YYSTACK_RELOCATE
# 1182|           if (yyss1 != yyssa)', true, 'Non-Issue', 'Bison generated code. LGTM', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.14124');
INSERT INTO public.ground_truth VALUES (286, 'libuser-0.64-7.el10', 7, 'Error: INTEGER_OVERFLOW (CWE-190):
libuser-0.64/modules/files.c:201: tainted_data_return: Called function "write(ofd, p, left)", and a possible return value may be less than zero.
libuser-0.64/modules/files.c:201: assign: Assigning: "out" = "write(ofd, p, left)".
libuser-0.64/modules/files.c:211: overflow: The expression "left" is considered to have possibly overflowed.
libuser-0.64/modules/files.c:201: overflow_sink: "left", which might have overflowed, is passed to "write(ofd, p, left)".
#  199|   			ssize_t out;
#  200|   
#  201|-> 			out = write(ofd, p, left);
#  202|   			if (out == -1) {
#  203|   				if (errno == EINTR)', true, 'Non-Issue', 'Only negative number that can be returned from write() is -1 and that is explicetly handled.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.14124');
INSERT INTO public.ground_truth VALUES (302, 'mpdecimal-2.5.1-9.el10', 15, 'Error: OVERRUN (CWE-119):
mpdecimal-2.5.1/libmpdec/mpdecimal.c:5747: cond_at_least: Checking "la <= 6442450944ULL" implies that "la" is at least 6442450945 on the false branch.
mpdecimal-2.5.1/libmpdec/mpdecimal.c:5765: assignment: Assigning: "m" = "(la + 1UL) / 2UL". The value of "m" is now at least 3221225473.
mpdecimal-2.5.1/libmpdec/mpdecimal.c:5827: overrun-buffer-arg: Calling "_mpd_basesubfrom" with "c + m" and "m + m" is suspicious because of the very large index, 6442450946. The index may be due to a negative parameter being interpreted as unsigned.
# 5825|       }
# 5826|       _mpd_baseaddto(c, w, m+m);
# 5827|->     _mpd_basesubfrom(c+m, w, m+m);
# 5828|   
# 5829|       return 1;', true, 'Non-Issue', 'The number is unsigned, hence the big value is expected there.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.145483');
INSERT INTO public.ground_truth VALUES (288, 'mpdecimal-2.5.1-9.el10', 1, 'Error: OVERRUN (CWE-119):
mpdecimal-2.5.1/libmpdec/mpdecimal.c:5665: return_constant: Function call "_mpd_get_transform_len(*rsize)" may return 6442450944.
mpdecimal-2.5.1/libmpdec/mpdecimal.c:5665: assignment: Assigning: "n" = "_mpd_get_transform_len(*rsize)". The value of "n" is now 6442450944.
mpdecimal-2.5.1/libmpdec/mpdecimal.c:5684: overrun-buffer-arg: Calling "fnt_autoconvolute" with "c3" and "n" is suspicious because of the very large index, 6442450944. The index may be due to a negative parameter being interpreted as unsigned.
# 5682|   
# 5683|       if (u == v) {
# 5684|->         if (!fnt_autoconvolute(c1, n, P1) ||
# 5685|               !fnt_autoconvolute(c2, n, P2) ||
# 5686|               !fnt_autoconvolute(c3, n, P3)) {', true, 'Issue', 'The function that allocates memory (mpd_sh_alloc) checks for integer overflows.', 'Large index value (6442450944) from `_mpd_get_transform_len(*rsize)` (line 5665) is assigned to `n` without explicit overflow checks, and subsequently used for memory allocation (lines 5669-5677) and `fnt_autoconvolute` calls (line 5684), potentially leading to a buffer overrun vulnerability (CWE-119).', '2025-11-18 16:18:56.145483');
INSERT INTO public.ground_truth VALUES (289, 'mpdecimal-2.5.1-9.el10', 2, 'Error: OVERRUN (CWE-119):
mpdecimal-2.5.1/libmpdec/mpdecimal.c:5665: return_constant: Function call "_mpd_get_transform_len(*rsize)" may return 6442450944.
mpdecimal-2.5.1/libmpdec/mpdecimal.c:5665: assignment: Assigning: "n" = "_mpd_get_transform_len(*rsize)". The value of "n" is now 6442450944.
mpdecimal-2.5.1/libmpdec/mpdecimal.c:5696: overrun-buffer-arg: Calling "fnt_convolute" with "c1" and "n" is suspicious because of the very large index, 6442450944. The index may be due to a negative parameter being interpreted as unsigned.
# 5694|   
# 5695|           memcpy(vtmp, v, vlen * (sizeof *vtmp));
# 5696|->         if (!fnt_convolute(c1, vtmp, n, P1)) {
# 5697|               mpd_free(vtmp);
# 5698|               goto malloc_error;', true, 'Non-Issue', 'The function that allocates memory (mpd_sh_alloc) checks for integer overflows.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.145483');
INSERT INTO public.ground_truth VALUES (290, 'mpdecimal-2.5.1-9.el10', 3, 'Error: UNINIT (CWE-457):
mpdecimal-2.5.1/libmpdec/mpdecimal.c:846: var_decl: Declaring variable "dummy" without initializer.
mpdecimal-2.5.1/libmpdec/mpdecimal.c:866: uninit_use_in_call: Using uninitialized value "dummy" when calling "mpd_qresize".
#  864|               len = _mpd_real_size(result->data, len);
#  865|               /* resize to fewer words cannot fail */
#  866|->             mpd_qresize(result, len, &dummy);
#  867|               result->len = len;
#  868|               mpd_setdigits(result);', true, 'Non-Issue', 'The variable dummy is not used', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.145483');
INSERT INTO public.ground_truth VALUES (291, 'mpdecimal-2.5.1-9.el10', 4, 'Error: OVERRUN (CWE-119):
mpdecimal-2.5.1/libmpdec/mpdecimal.c:5747: cond_at_least: Checking "la <= 6442450944ULL" implies that "la" is at least 6442450945 on the false branch.
mpdecimal-2.5.1/libmpdec/mpdecimal.c:5765: assignment: Assigning: "m" = "(la + 1UL) / 2UL". The value of "m" is now at least 3221225473.
mpdecimal-2.5.1/libmpdec/mpdecimal.c:5826: overrun-buffer-arg: Calling "_mpd_baseaddto" with "w" and "m + m" is suspicious because of the very large index, 6442450946. The index may be due to a negative parameter being interpreted as unsigned.
# 5824|           return 0; /* GCOV_UNLIKELY */
# 5825|       }
# 5826|->     _mpd_baseaddto(c, w, m+m);
# 5827|       _mpd_basesubfrom(c+m, w, m+m);
# 5828|', true, 'Non-Issue', 'The number is unsigned, hence the big value is expected there.', 'The reported issue is deemed a FALSE POSITIVE because the large indices (e.g., `6442450946`) are explicitly unsigned, and the code''s logic (e.g., line 5765''s assignment) and function calls (e.g., `_mpd_baseaddto` at line 5826) are designed to handle such values within the unsigned integer range, without introducing a buffer overrun vulnerability.', '2025-11-18 16:18:56.145483');
INSERT INTO public.ground_truth VALUES (292, 'mpdecimal-2.5.1-9.el10', 5, 'Error: OVERRUN (CWE-119):
mpdecimal-2.5.1/libmpdec/mpdecimal.c:5747: cond_at_least: Checking "la <= 6442450944ULL" implies that "la" is at least 6442450945 on the false branch.
mpdecimal-2.5.1/libmpdec/mpdecimal.c:5765: assignment: Assigning: "m" = "(la + 1UL) / 2UL". The value of "m" is now at least 3221225473.
mpdecimal-2.5.1/libmpdec/mpdecimal.c:5826: overrun-buffer-arg: Calling "_mpd_baseaddto" with "c" and "m + m" is suspicious because of the very large index, 6442450946. The index may be due to a negative parameter being interpreted as unsigned.
# 5824|           return 0; /* GCOV_UNLIKELY */
# 5825|       }
# 5826|->     _mpd_baseaddto(c, w, m+m);
# 5827|       _mpd_basesubfrom(c+m, w, m+m);
# 5828|', true, 'Non-Issue', 'The number is unsigned, hence the big value is expected there.', 'The reported issue is deemed a FALSE POSITIVE because the large indices (e.g., `6442450946`) are explicitly unsigned, and the code''s logic (e.g., line 5765''s assignment) and function calls (e.g., `_mpd_baseaddto` at line 5826) are designed to handle such values within the unsigned integer range, without introducing a buffer overrun vulnerability.', '2025-11-18 16:18:56.145483');
INSERT INTO public.ground_truth VALUES (293, 'mpdecimal-2.5.1-9.el10', 6, 'Error: OVERRUN (CWE-119):
mpdecimal-2.5.1/libmpdec/mpdecimal.c:5665: return_constant: Function call "_mpd_get_transform_len(*rsize)" may return 6442450944.
mpdecimal-2.5.1/libmpdec/mpdecimal.c:5665: assignment: Assigning: "n" = "_mpd_get_transform_len(*rsize)". The value of "n" is now 6442450944.
mpdecimal-2.5.1/libmpdec/mpdecimal.c:5703: overrun-buffer-arg: Calling "fnt_convolute" with "vtmp" and "n" is suspicious because of the very large index, 6442450944. The index may be due to a negative parameter being interpreted as unsigned.
# 5701|           memcpy(vtmp, v, vlen * (sizeof *vtmp));
# 5702|           mpd_uint_zero(vtmp+vlen, n-vlen);
# 5703|->         if (!fnt_convolute(c2, vtmp, n, P2)) {
# 5704|               mpd_free(vtmp);
# 5705|               goto malloc_error;', true, 'Non-Issue', 'The function that allocates memory (mpd_sh_alloc) checks for integer overflows.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.145483');
INSERT INTO public.ground_truth VALUES (294, 'mpdecimal-2.5.1-9.el10', 7, 'Error: OVERRUN (CWE-119):
mpdecimal-2.5.1/libmpdec/mpdecimal.c:5747: cond_at_least: Checking "la <= 6442450944ULL" implies that "la" is at least 6442450945 on the false branch.
mpdecimal-2.5.1/libmpdec/mpdecimal.c:5765: assignment: Assigning: "m" = "(la + 1UL) / 2UL". The value of "m" is now at least 3221225473.
mpdecimal-2.5.1/libmpdec/mpdecimal.c:5802: overrun-buffer-arg: Calling "memcpy" with "w + (m + 1UL)" and "m * 8UL" is suspicious because of the very large index, 25769803784. The index may be due to a negative parameter being interpreted as unsigned. [Note: The source code implementation of the function has been overridden by a builtin model.]
# 5800|       _mpd_baseaddto(w, a+m, la-m);
# 5801|   
# 5802|->     memcpy(w+(m+1), b, m * sizeof *w);
# 5803|       w[m+1+m] = 0;
# 5804|       _mpd_baseaddto(w+(m+1), b+m, lb-m);', true, 'Non-Issue', 'The number is unsigned, hence the big value is expected there.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.145483');
INSERT INTO public.ground_truth VALUES (433, 'sqlite-3.45.1-2.el10', 102, 'Error: UNINIT (CWE-457):
sqlite-src-3450100/sqlite3_analyzer.c:204943: skipped_decl: Jumping over declaration of "seenE".
sqlite-src-3450100/sqlite3_analyzer.c:205048: uninit_use: Using uninitialized value "seenE".
#205046|             }
#205047|           }
#205048|->         if( seenE ){
#205049|             pParse->iErr = j;
#205050|             return -1;', true, 'Non-Issue', 'seenE is set ahead every jump to parse_number_2', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (295, 'mpdecimal-2.5.1-9.el10', 8, 'Error: OVERRUN (CWE-119):
mpdecimal-2.5.1/libmpdec/mpdecimal.c:5747: cond_at_least: Checking "la <= 6442450944ULL" implies that "la" is at least 6442450945 on the false branch.
mpdecimal-2.5.1/libmpdec/mpdecimal.c:5765: assignment: Assigning: "m" = "(la + 1UL) / 2UL". The value of "m" is now at least 3221225473.
mpdecimal-2.5.1/libmpdec/mpdecimal.c:5787: assignment: Assigning: "lt" = "m + m + 1UL". The value of "lt" is now at least 6442450947.
mpdecimal-2.5.1/libmpdec/mpdecimal.c:5788: overrun-buffer-arg: Calling "mpd_uint_zero" with "w" and "lt" is suspicious because of the very large index, 6442450947. The index may be due to a negative parameter being interpreted as unsigned.
# 5786|   
# 5787|           lt = m + m + 1;         /* space needed for the result array */
# 5788|->         mpd_uint_zero(w, lt);   /* clear result array */
# 5789|           if (!_karatsuba_rec_fnt(w, a, b, w+lt, m, lb)) {  /* al*b */
# 5790|               return 0; /* GCOV_UNLIKELY */', true, 'Non-Issue', 'The number is unsigned, hence the big value is expected there.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.145483');
INSERT INTO public.ground_truth VALUES (296, 'mpdecimal-2.5.1-9.el10', 9, 'Error: COPY_PASTE_ERROR (CWE-398):
mpdecimal-2.5.1/libmpdec/mpdecimal.c:7626: original: "_mpd_qadd_exact(qq, qq, &minus_one, &workctx, &workctx.status)" looks like the original copy.
mpdecimal-2.5.1/libmpdec/mpdecimal.c:7635: copy_paste_error: "_mpd_qadd_exact" in "_mpd_qadd_exact(qq, qq, &one, &workctx, &workctx.status)" looks like a copy-paste error.
mpdecimal-2.5.1/libmpdec/mpdecimal.c:7635: remediation: Should it say "_mpd_qsub_exact" instead?
# 7633|           else {
# 7634|               _mpd_qsub_exact(rr, rr, &bb, &workctx, &workctx.status);
# 7635|->             _mpd_qadd_exact(qq, qq, &one, &workctx, &workctx.status);
# 7636|           }
# 7637|       }', true, 'Non-Issue', 'The code is correct', 'The `_mpd_qadd_exact` at line 7635 is not a copy-paste error, but a deliberate increment of the quotient (`qq`) by 1, logically following the remainder''s adjustment at line 7634, aligning with the division correction loop''s purpose (lines 7633-7637).', '2025-11-18 16:18:56.145483');
INSERT INTO public.ground_truth VALUES (297, 'mpdecimal-2.5.1-9.el10', 10, 'Error: OVERRUN (CWE-119):
mpdecimal-2.5.1/libmpdec/mpdecimal.c:5665: return_constant: Function call "_mpd_get_transform_len(*rsize)" may return 6442450944.
mpdecimal-2.5.1/libmpdec/mpdecimal.c:5665: assignment: Assigning: "n" = "_mpd_get_transform_len(*rsize)". The value of "n" is now 6442450944.
mpdecimal-2.5.1/libmpdec/mpdecimal.c:5696: overrun-buffer-arg: Calling "fnt_convolute" with "vtmp" and "n" is suspicious because of the very large index, 6442450944. The index may be due to a negative parameter being interpreted as unsigned.
# 5694|   
# 5695|           memcpy(vtmp, v, vlen * (sizeof *vtmp));
# 5696|->         if (!fnt_convolute(c1, vtmp, n, P1)) {
# 5697|               mpd_free(vtmp);
# 5698|               goto malloc_error;', true, 'Non-Issue', 'The function that allocates memory (mpd_sh_alloc) checks for integer overflows.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.145483');
INSERT INTO public.ground_truth VALUES (298, 'mpdecimal-2.5.1-9.el10', 11, 'Error: OVERRUN (CWE-119):
mpdecimal-2.5.1/libmpdec/mpdecimal.c:5747: cond_at_least: Checking "la <= 6442450944ULL" implies that "la" is at least 6442450945 on the false branch.
mpdecimal-2.5.1/libmpdec/mpdecimal.c:5765: assignment: Assigning: "m" = "(la + 1UL) / 2UL". The value of "m" is now at least 3221225473.
mpdecimal-2.5.1/libmpdec/mpdecimal.c:5798: overrun-buffer-arg: Calling "memcpy" with "w" and "m * 8UL" is suspicious because of the very large index, 25769803784. The index may be due to a negative parameter being interpreted as unsigned. [Note: The source code implementation of the function has been overridden by a builtin model.]
# 5796|   
# 5797|       /* la >= lb > m */
# 5798|->     memcpy(w, a, m * sizeof *w);
# 5799|       w[m] = 0;
# 5800|       _mpd_baseaddto(w, a+m, la-m);', true, 'Non-Issue', 'The number is unsigned, hence the big value is expected there.', 'The `memcpy` call at line 5798 with a large index is deemed safe since `m` (of type `mpd_size_t`, an unsigned type) is intentionally calculated as an unsigned value (`m = (la+1)/2;` at line 5765), making the large index an expected behavior rather than a sign of buffer overrun due to signed-unsigned interpretation.', '2025-11-18 16:18:56.145483');
INSERT INTO public.ground_truth VALUES (299, 'mpdecimal-2.5.1-9.el10', 12, 'Error: OVERRUN (CWE-119):
mpdecimal-2.5.1/libmpdec/mpdecimal.c:5665: return_constant: Function call "_mpd_get_transform_len(*rsize)" may return 6442450944.
mpdecimal-2.5.1/libmpdec/mpdecimal.c:5665: assignment: Assigning: "n" = "_mpd_get_transform_len(*rsize)". The value of "n" is now 6442450944.
mpdecimal-2.5.1/libmpdec/mpdecimal.c:5710: overrun-buffer-arg: Calling "fnt_convolute" with "c3" and "n" is suspicious because of the very large index, 6442450944. The index may be due to a negative parameter being interpreted as unsigned.
# 5708|           memcpy(vtmp, v, vlen * (sizeof *vtmp));
# 5709|           mpd_uint_zero(vtmp+vlen, n-vlen);
# 5710|->         if (!fnt_convolute(c3, vtmp, n, P3)) {
# 5711|               mpd_free(vtmp);
# 5712|               goto malloc_error;', true, 'Issue', 'The function that allocates memory (mpd_sh_alloc) checks for integer overflows.', 'Lack of explicit overflow check in `_mpd_get_transform_len(*rsize)` and unclear behavior of this function, combined with potential for buffer overrun in subsequent memory operations (e.g., `memcpy` and `mpd_uint_zero`), directly correlate with CWE-119 (Buffer Overrun) without sufficient evidence for a FALSE POSITIVE.', '2025-11-18 16:18:56.145483');
INSERT INTO public.ground_truth VALUES (300, 'mpdecimal-2.5.1-9.el10', 13, 'Error: OVERRUN (CWE-119):
mpdecimal-2.5.1/libmpdec/mpdecimal.c:5747: cond_at_least: Checking "la <= 6442450944ULL" implies that "la" is at least 6442450945 on the false branch.
mpdecimal-2.5.1/libmpdec/mpdecimal.c:5765: assignment: Assigning: "m" = "(la + 1UL) / 2UL". The value of "m" is now at least 3221225473.
mpdecimal-2.5.1/libmpdec/mpdecimal.c:5827: overrun-buffer-arg: Calling "_mpd_basesubfrom" with "w" and "m + m" is suspicious because of the very large index, 6442450946. The index may be due to a negative parameter being interpreted as unsigned.
# 5825|       }
# 5826|       _mpd_baseaddto(c, w, m+m);
# 5827|->     _mpd_basesubfrom(c+m, w, m+m);
# 5828|   
# 5829|       return 1;', true, 'Non-Issue', 'The number is unsigned, hence the big value is expected there.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.145483');
INSERT INTO public.ground_truth VALUES (301, 'mpdecimal-2.5.1-9.el10', 14, 'Error: INTEGER_OVERFLOW (CWE-190):
mpdecimal-2.5.1/libmpdec/mpdecimal.c:3078: tainted_data_return: Called function "mpd_arith_sign(b)", and a possible return value may be less than zero.
mpdecimal-2.5.1/libmpdec/mpdecimal.c:3078: overflow: The expression "a->exp + (int64_t)n * mpd_arith_sign(b)" is considered to have possibly overflowed.
mpdecimal-2.5.1/libmpdec/mpdecimal.c:3078: assign: Assigning: "exp" = "a->exp + (int64_t)n * mpd_arith_sign(b)".
mpdecimal-2.5.1/libmpdec/mpdecimal.c:3080: cast_overflow: An assign that casts to a different type, which might trigger an overflow.
mpdecimal-2.5.1/libmpdec/mpdecimal.c:3100: cast_overflow: An assign that casts to a different type, which might trigger an overflow.
mpdecimal-2.5.1/libmpdec/mpdecimal.c:3102: overflow_sink: "result->exp", which might have overflowed, is passed to "mpd_qfinalize(result, ctx, status)".
# 3100|       result->exp = (mpd_ssize_t)exp;
# 3101|   
# 3102|->     mpd_qfinalize(result, ctx, status);
# 3103|   }
# 3104|', true, 'Non-Issue', 'We cast the unsigned n variable to a signed type (int64_t) so it cannot overflow.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.145483');
INSERT INTO public.ground_truth VALUES (303, 'mpdecimal-2.5.1-9.el10', 16, 'Error: OVERRUN (CWE-119):
mpdecimal-2.5.1/libmpdec/mpdecimal.c:1005: assignment: Assigning: "n" = "mpd_word_digits(word)". The value of "n" is now between 1 and 20 (inclusive).
mpdecimal-2.5.1/libmpdec/mpdecimal.c:1006: overrun-local: Overrunning array "mpd_pow10" of 20 8-byte elements at element index 20 (byte offset 167) using index "n" (which evaluates to 20).
# 1004|   
# 1005|       n = mpd_word_digits(word);
# 1006|->     if (word == mpd_pow10[n]-1) {
# 1007|           return 1;
# 1008|       }', false, 'Non-Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-44991

We might overrun the array.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.145483');
INSERT INTO public.ground_truth VALUES (304, 'mpdecimal-2.5.1-9.el10', 17, 'Error: OVERRUN (CWE-119):
mpdecimal-2.5.1/libmpdec/mpdecimal.c:5747: cond_at_least: Checking "la <= 6442450944ULL" implies that "la" is at least 6442450945 on the false branch.
mpdecimal-2.5.1/libmpdec/mpdecimal.c:5765: assignment: Assigning: "m" = "(la + 1UL) / 2UL". The value of "m" is now at least 3221225473.
mpdecimal-2.5.1/libmpdec/mpdecimal.c:5798: overrun-buffer-arg: Calling "memcpy" with "a" and "m * 8UL" is suspicious because of the very large index, 25769803784. The index may be due to a negative parameter being interpreted as unsigned. [Note: The source code implementation of the function has been overridden by a builtin model.]
# 5796|   
# 5797|       /* la >= lb > m */
# 5798|->     memcpy(w, a, m * sizeof *w);
# 5799|       w[m] = 0;
# 5800|       _mpd_baseaddto(w, a+m, la-m);', true, 'Non-Issue', 'The number is unsigned, hence the big value is expected there.', 'The large index value in `memcpy` at line 5798 is expected due to the unsigned nature of `m` (defined as `mpd_size_t` at line 5742) and its calculation `m = (la+1)/2;` (line 5765), ruling out a negative-to-unsigned misinterpretation buffer overrun risk.', '2025-11-18 16:18:56.145483');
INSERT INTO public.ground_truth VALUES (305, 'mpdecimal-2.5.1-9.el10', 18, 'Error: OVERRUN (CWE-119):
mpdecimal-2.5.1/libmpdec/mpdecimal.c:5665: return_constant: Function call "_mpd_get_transform_len(*rsize)" may return 6442450944.
mpdecimal-2.5.1/libmpdec/mpdecimal.c:5665: assignment: Assigning: "n" = "_mpd_get_transform_len(*rsize)". The value of "n" is now 6442450944.
mpdecimal-2.5.1/libmpdec/mpdecimal.c:5684: overrun-buffer-arg: Calling "fnt_autoconvolute" with "c2" and "n" is suspicious because of the very large index, 6442450944. The index may be due to a negative parameter being interpreted as unsigned.
# 5682|   
# 5683|       if (u == v) {
# 5684|->         if (!fnt_autoconvolute(c1, n, P1) ||
# 5685|               !fnt_autoconvolute(c2, n, P2) ||
# 5686|               !fnt_autoconvolute(c3, n, P3)) {', true, 'Issue', 'The function that allocates memory (mpd_sh_alloc) checks for integer overflows.', 'No explicit overflow checks are performed on `n` (assigned from `_mpd_get_transform_len(*rsize)` at line 5665) before using it for memory allocation (lines 5669-5677) and `fnt_autoconvolute` calls (lines 5684-5686), potentially leading to buffer overruns or memory allocation issues.', '2025-11-18 16:18:56.145483');
INSERT INTO public.ground_truth VALUES (306, 'mpdecimal-2.5.1-9.el10', 19, 'Error: UNINIT (CWE-457):
mpdecimal-2.5.1/libmpdec/mpdecimal.c:817: var_decl: Declaring variable "dummy" without initializer.
mpdecimal-2.5.1/libmpdec/mpdecimal.c:830: uninit_use_in_call: Using uninitialized value "dummy" when calling "mpd_qresize".
#  828|           len = _mpd_real_size(result->data, len);
#  829|           /* resize to fewer words cannot fail */
#  830|->         mpd_qresize(result, len, &dummy);
#  831|           result->len = len;
#  832|           mpd_setdigits(result);', true, 'Non-Issue', 'The variable dummy is not used', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.145483');
INSERT INTO public.ground_truth VALUES (307, 'mpdecimal-2.5.1-9.el10', 20, 'Error: OVERRUN (CWE-119):
mpdecimal-2.5.1/libmpdec/mpdecimal.c:5665: return_constant: Function call "_mpd_get_transform_len(*rsize)" may return 6442450944.
mpdecimal-2.5.1/libmpdec/mpdecimal.c:5665: assignment: Assigning: "n" = "_mpd_get_transform_len(*rsize)". The value of "n" is now 6442450944.
mpdecimal-2.5.1/libmpdec/mpdecimal.c:5703: overrun-buffer-arg: Calling "fnt_convolute" with "c2" and "n" is suspicious because of the very large index, 6442450944. The index may be due to a negative parameter being interpreted as unsigned.
# 5701|           memcpy(vtmp, v, vlen * (sizeof *vtmp));
# 5702|           mpd_uint_zero(vtmp+vlen, n-vlen);
# 5703|->         if (!fnt_convolute(c2, vtmp, n, P2)) {
# 5704|               mpd_free(vtmp);
# 5705|               goto malloc_error;', true, 'Issue', 'The function that allocates memory (mpd_sh_alloc) checks for integer overflows.', 'Large index value (6442450944) from `_mpd_get_transform_len(*rsize)` at line 5665 is used without explicit overflow checks in `mpd_uint_zero` (line 5702) and `fnt_convolute` (line 5703), potentially leading to a buffer overrun, with no visible integer overflow protection in the provided code snippet.', '2025-11-18 16:18:56.145483');
INSERT INTO public.ground_truth VALUES (308, 'mpdecimal-2.5.1-9.el10', 21, 'Error: OVERRUN (CWE-119):
mpdecimal-2.5.1/libmpdec/mpdecimal.c:5665: return_constant: Function call "_mpd_get_transform_len(*rsize)" may return 6442450944.
mpdecimal-2.5.1/libmpdec/mpdecimal.c:5665: assignment: Assigning: "n" = "_mpd_get_transform_len(*rsize)". The value of "n" is now 6442450944.
mpdecimal-2.5.1/libmpdec/mpdecimal.c:5684: overrun-buffer-arg: Calling "fnt_autoconvolute" with "c1" and "n" is suspicious because of the very large index, 6442450944. The index may be due to a negative parameter being interpreted as unsigned.
# 5682|   
# 5683|       if (u == v) {
# 5684|->         if (!fnt_autoconvolute(c1, n, P1) ||
# 5685|               !fnt_autoconvolute(c2, n, P2) ||
# 5686|               !fnt_autoconvolute(c3, n, P3)) {', true, 'Non-Issue', 'The function that allocates memory (mpd_sh_alloc) checks for integer overflows.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.145483');
INSERT INTO public.ground_truth VALUES (309, 'mpdecimal-2.5.1-9.el10', 22, 'Error: UNINIT (CWE-457):
mpdecimal-2.5.1/libmpdec/mpdecimal.c:2594: var_decl: Declaring variable "dummy" without initializer.
mpdecimal-2.5.1/libmpdec/mpdecimal.c:2614: uninit_use_in_call: Using uninitialized value "dummy" when calling "mpd_qresize".
# 2612|           size = mpd_digits_to_size(result->digits);
# 2613|           /* reducing the size cannot fail */
# 2614|->         mpd_qresize(result, size, &dummy);
# 2615|           result->len = size;
# 2616|       }', true, 'Non-Issue', 'The variable dummy is not used', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.145483');
INSERT INTO public.ground_truth VALUES (310, 'mpdecimal-2.5.1-9.el10', 23, 'Error: OVERRUN (CWE-119):
mpdecimal-2.5.1/libmpdec/mpdecimal.c:5747: cond_at_least: Checking "la <= 6442450944ULL" implies that "la" is at least 6442450945 on the false branch.
mpdecimal-2.5.1/libmpdec/mpdecimal.c:5765: assignment: Assigning: "m" = "(la + 1UL) / 2UL". The value of "m" is now at least 3221225473.
mpdecimal-2.5.1/libmpdec/mpdecimal.c:5820: assignment: Assigning: "lt" = "m + m + 1UL". The value of "lt" is now at least 6442450947.
mpdecimal-2.5.1/libmpdec/mpdecimal.c:5821: overrun-buffer-arg: Calling "mpd_uint_zero" with "w" and "lt" is suspicious because of the very large index, 6442450947. The index may be due to a negative parameter being interpreted as unsigned.
# 5819|   
# 5820|       lt = m + m + 1;
# 5821|->     mpd_uint_zero(w, lt);
# 5822|   
# 5823|       if (!_karatsuba_rec_fnt(w, a, b, w+lt, m, m)) {', true, 'Non-Issue', 'The number is unsigned, hence the big value is expected there.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.145483');
INSERT INTO public.ground_truth VALUES (311, 'mpdecimal-2.5.1-9.el10', 24, 'Error: OVERRUN (CWE-119):
mpdecimal-2.5.1/libmpdec/mpdecimal.c:5665: return_constant: Function call "_mpd_get_transform_len(*rsize)" may return 6442450944.
mpdecimal-2.5.1/libmpdec/mpdecimal.c:5665: assignment: Assigning: "n" = "_mpd_get_transform_len(*rsize)". The value of "n" is now 6442450944.
mpdecimal-2.5.1/libmpdec/mpdecimal.c:5710: overrun-buffer-arg: Calling "fnt_convolute" with "vtmp" and "n" is suspicious because of the very large index, 6442450944. The index may be due to a negative parameter being interpreted as unsigned.
# 5708|           memcpy(vtmp, v, vlen * (sizeof *vtmp));
# 5709|           mpd_uint_zero(vtmp+vlen, n-vlen);
# 5710|->         if (!fnt_convolute(c3, vtmp, n, P3)) {
# 5711|               mpd_free(vtmp);
# 5712|               goto malloc_error;', true, 'Non-Issue', 'The function that allocates memory (mpd_sh_alloc) checks for integer overflows.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.145483');
INSERT INTO public.ground_truth VALUES (312, 'mpdecimal-2.5.1-9.el10', 25, 'Error: OVERRUN (CWE-119):
mpdecimal-2.5.1/libmpdec/mpdecimal.c:5747: cond_at_least: Checking "la <= 6442450944ULL" implies that "la" is at least 6442450945 on the false branch.
mpdecimal-2.5.1/libmpdec/mpdecimal.c:5765: assignment: Assigning: "m" = "(la + 1UL) / 2UL". The value of "m" is now at least 3221225473.
mpdecimal-2.5.1/libmpdec/mpdecimal.c:5802: overrun-buffer-arg: Calling "memcpy" with "b" and "m * 8UL" is suspicious because of the very large index, 25769803784. The index may be due to a negative parameter being interpreted as unsigned. [Note: The source code implementation of the function has been overridden by a builtin model.]
# 5800|       _mpd_baseaddto(w, a+m, la-m);
# 5801|   
# 5802|->     memcpy(w+(m+1), b, m * sizeof *w);
# 5803|       w[m+1+m] = 0;
# 5804|       _mpd_baseaddto(w+(m+1), b+m, lb-m);', true, 'Non-Issue', 'The number is unsigned, hence the big value is expected there.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.145483');
INSERT INTO public.ground_truth VALUES (313, 'nano-7.2-6.el10', 1, 'Error: USE_AFTER_FREE (CWE-672):
nano-7.2/src/files.c:1948: freed_arg: "copy_file" frees "thefile".
nano-7.2/src/files.c:1952: use_closed_file: Calling "fclose" uses file handle "thefile" after closing it.
# 1950|   		if (verdict < 0) {
# 1951|   			statusline(ALERT, _("Error reading temp file: %s"), strerror(errno));
# 1952|-> 			fclose(thefile);
# 1953|   			goto cleanup_and_exit;
# 1954|   		} else if (verdict > 0) {', true, 'Non-Issue', '`copy_file` copies `source` to `thefile` and the third argument decides if `thefile` should be automatically closed after the copying is done.  Since this flag is set to `FALSE`, `thefile` is still open when `copy_file` returns and `fclose` is, therefore, called only once.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.152762');
INSERT INTO public.ground_truth VALUES (314, 'nano-7.2-6.el10', 2, 'Error: UNINIT (CWE-457):
nano-7.2/src/browser.c:390: var_decl: Declaring variable "dir" without initializer.
nano-7.2/src/browser.c:417: uninit_use: Using uninitialized value "dir".
#  415|           }
#  416|   
#  417|->         if (dir != NULL) {
#  418|                   /* Get the file list, and set gauge and piles in the process. */
#  419|                   read_the_list(path, dir);', false, 'Non-Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-36769

Note to self: Add `NULL` initializer for the `dir` pointer.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.152762');
INSERT INTO public.ground_truth VALUES (315, 'nano-7.2-6.el10', 3, 'Error: USE_AFTER_FREE (CWE-672):
nano-7.2/src/files.c:1948: freed_arg: "copy_file" frees "thefile".
nano-7.2/src/files.c:1956: use_closed_file: Calling "fclose" uses file handle "thefile" after closing it.
# 1954|   		} else if (verdict > 0) {
# 1955|   			statusline(ALERT, _("Error writing %s: %s"), realname, strerror(errno));
# 1956|-> 			fclose(thefile);
# 1957|   			goto cleanup_and_exit;
# 1958|   		}', true, 'Non-Issue', '`copy_file` copies `source` to `thefile` and the third argument decides if `thefile` should be automatically closed after the copying is done.  Since this flag is set to `FALSE`, `thefile` is still open when `copy_file` returns and `fclose` is, therefore, called only once.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.152762');
INSERT INTO public.ground_truth VALUES (316, 'nano-7.2-6.el10', 4, 'Error: RESOURCE_LEAK (CWE-772):
nano-7.2/src/files.c:177: open_fn: Returning handle opened by "open". [Note: The source code implementation of the function has been overridden by a user model.]
nano-7.2/src/files.c:177: var_assign: Assigning: "fd" = handle returned from "open(lockfilename, 193, 438)".
nano-7.2/src/files.c:179: off_by_one: Testing whether handle "fd" is strictly greater than zero is suspicious.  "fd" leaks when it is zero.
nano-7.2/src/files.c:179: remediation: Did you intend to include equality with zero?
nano-7.2/src/files.c:185: off_by_one: Testing whether handle "fd" is strictly greater than zero is suspicious.  "fd" leaks when it is zero.
nano-7.2/src/files.c:185: remediation: Did you intend to include equality with zero?
nano-7.2/src/files.c:187: leaked_handle: Handle variable "fd" going out of scope leaks the handle.
#  185|   		if (fd > 0)
#  186|   			close(fd);
#  187|-> 		return FALSE;
#  188|   	}
#  189|', true, 'Non-Issue', 'I''m 99% percent sure that this is a false positive because nano expects that fd 0 points to a tty (or pipe if `-` is passed one of the arguments) and that it will be open during its whole execution.  If stdin is closed before nano starts, it will produce an error message and exit immediately.  Therefore, all `open` calls in nano should either return `-1` or a valid fd greater than 0.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.152762');
INSERT INTO public.ground_truth VALUES (317, 'nano-7.2-6.el10', 5, 'Error: RESOURCE_LEAK (CWE-772):
nano-7.2/src/text.c:2055: alloc_arg: "open_file" allocates memory that is stored into "stream".
nano-7.2/src/text.c:2058: leaked_storage: Variable "stream" going out of scope leaks the storage it points to.
# 2056|   
# 2057|   	if (descriptor < 0)
# 2058|-> 		return FALSE;
# 2059|   
# 2060|   #ifndef NANO_TINY', true, 'Non-Issue', 'open_file returns -1 only on failure and resources allocated by that function are properly freed in such case.  Therefore, will not point to an open stream.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.152762');
INSERT INTO public.ground_truth VALUES (332, 'sqlite-3.45.1-2.el10', 1, 'Error: OVERLAPPING_COPY:
sqlite-src-3450100/sqlite3_analyzer.c:95826: assign: Assigning: "pIn1" = "&aMem[pOp->p1]".
sqlite-src-3450100/sqlite3_analyzer.c:95865: equal: The address of "pIn1->u.r" is equal to "aMem + pOp->p1".
sqlite-src-3450100/sqlite3_analyzer.c:95865: equal: The address of "pIn1->u.i" is equal to "aMem + pOp->p1".
sqlite-src-3450100/sqlite3_analyzer.c:95865: overlapping_assignment: Assigning "pIn1->u.i" to "pIn1->u.r", which have overlapping memory locations and different types.
sqlite-src-3450100/sqlite3_analyzer.c:95865: target_type: "pIn1->u.r" has type "double".
sqlite-src-3450100/sqlite3_analyzer.c:95865: source_type: "pIn1->u.i" has type "long long".
#95863|                 pIn1->flags &= ~MEM_Int;
#95864|               }else{
#95865|->               pIn1->u.r = (double)pIn1->u.i;
#95866|                 pIn1->flags |= MEM_Real;
#95867|                 pIn1->flags &= ~MEM_Int;', true, 'Issue', 'Both u.r and u.i are members of the same union.', 'Assignment at line 95865 (`pIn1->u.r = (double)pIn1->u.i;`) involves overlapping memory locations with different types (`double` and `long long`), indicating a potential type punning vulnerability (CWE-698), with no clear mitigating evidence in the provided code.', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (319, 'nano-7.2-6.el10', 7, 'Error: COMPILER_WARNING:
nano-7.2/src/history.c: scope_hint: In function ‘save_poshistory’
nano-7.2/src/history.c:456:44: warning[-Wstringop-overflow=]: writing 1 byte into a region of size 0
#  456 |                 path_and_place[length - 1] = ''\n'';
#      |                                            ^
nano-7.2/src/utils.c:293:25: note: at offset -1 into destination object of size [44, 9223372036854775807] allocated by ‘malloc’
#  293 |         void *section = malloc(howmuch);
#      |                         ^
#  454|   		length = recode_LF_to_NUL(path_and_place);
#  455|   		/* Restore the terminating newline. */
#  456|-> 		path_and_place[length - 1] = ''\n'';
#  457|   
#  458|   		if (fwrite(path_and_place, 1, length, histfile) < length)', true, 'Issue', 'nmalloc guaranteed that the returned pointer is not NULL. The length of the allocated memory is enough to store the `snprintf`result (the size itself is enough is rather well explained in an accompanying comment).  Note that all filesystems that I know do not allow `NUL` bytes in file names.  Therefore it is safe to assume that `path_and_place` contins only a single `NUL` byte at ots end.  The `recode_LF_to_NUL` function will just replace all `\n` chacters with `\0` in the string and return the distance betweent its begining and the last `\0` byte.  Since the original string contained only one NUL charater and has to be at least 44 bytes long (as shown by the nmalloc call).  Therefore, `legnth` is greater than 43.  Hence, `length - 1` is a positive and valid index.', 'No explicit check ensures `length > 0` before writing to `path_and_place[length - 1]` at `history.c:456`, potentially leading to an out-of-bounds write if `recode_LF_to_NUL` returns 0, aligning with the warning of writing into a region of size 0.', '2025-11-18 16:18:56.152762');
INSERT INTO public.ground_truth VALUES (320, 'nano-7.2-6.el10', 8, 'Error: RESOURCE_LEAK (CWE-772):
nano-7.2/src/files.c:461: alloc_arg: "open_file" allocates memory that is stored into "f".
nano-7.2/src/files.c:492: leaked_storage: Variable "f" going out of scope leaks the storage it points to.
#  490|   
#  491|   	free(realname);
#  492|-> 	return TRUE;
#  493|   }
#  494|', true, 'Non-Issue', '`f` will point an open file stream if and only if `open_file` returns value greater than 0.  This handle is then passed to the `read_file` function and this function calles `fclose(f)` and all possible code paths to this line from its own entry point.  ', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.152762');
INSERT INTO public.ground_truth VALUES (321, 'nano-7.2-6.el10', 9, 'Error: RESOURCE_LEAK (CWE-772):
nano-7.2/src/text.c:2031: alloc_fn: Storage is returned from allocation function "copy_of".
nano-7.2/src/text.c:2031: var_assign: Assigning: "copy_of_command" = storage returned from "copy_of(command)".
nano-7.2/src/text.c:2032: noescape: Resource "copy_of_command" is not freed or pointed-to in "strtok".
nano-7.2/src/text.c:2043: leaked_storage: Variable "copy_of_command" going out of scope leaks the storage it points to.
# 2041|   	(*arguments)[count - 2] = filename;
# 2042|   	(*arguments)[count - 1] = NULL;
# 2043|-> }
# 2044|   #endif
# 2045|', true, 'Non-Issue', 'The leak is entended because this function duplicates and splits the `command` string into an array of strings that will be passed to `execve(2)`.  Nano will then fork.  If the given `execve(2)` calls in its chidren succeed, there is nothing to free and if they fail, `nano` will immediately call `exit(3)`.  Otherwise, `the parent process will correctly free the string regardless of `fork(2)`''s return value.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.152762');
INSERT INTO public.ground_truth VALUES (322, 'nano-7.2-6.el10', 10, 'Error: USE_AFTER_FREE (CWE-672):
nano-7.2/src/files.c:1676: freed_arg: "copy_file" frees "backup_file".
nano-7.2/src/files.c:1680: use_closed_file: Calling "fclose" uses file handle "backup_file" after closing it.
# 1678|   	if (original == NULL || verdict < 0) {
# 1679|   		warn_and_briefly_pause(_("Cannot read original file"));
# 1680|-> 		fclose(backup_file);
# 1681|   		goto failure;
# 1682|   	} else if (verdict > 0) {', true, 'Non-Issue', '`copy_file` copies `original` to `backup_file` and the third argument decides if `backup_file` should be automatically closed after the copying is done.  Since this flag is set to `FALSE`, `backup_file` is still open when `copy_file` returns and `fclose` is, therefore, called only once.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.152762');
INSERT INTO public.ground_truth VALUES (323, 'nano-7.2-6.el10', 11, 'Error: BAD_ALLOC_STRLEN (CWE-131):
nano-7.2/src/files.c:1409: bad_alloc_strlen: Using "strlen(slash + 1)" instead of "strlen(slash) + 1" as an argument to "nrealloc" might be an under-allocation.
# 1407|   		/* Upon success, re-add the last component of the original path. */
# 1408|   		if (target) {
# 1409|-> 			target = nrealloc(target, strlen(target) + strlen(slash + 1) + 1);
# 1410|   			strcat(target, slash + 1);
# 1411|   		}', true, 'Non-Issue', 'The next line appends the string starting at `slash + 1` to the `target` string.  Therefore, it is correct to use `strlen(slash + 1)` to compule length of this substring.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.152762');
INSERT INTO public.ground_truth VALUES (324, 'nano-7.2-6.el10', 12, 'Error: RESOURCE_LEAK (CWE-772):
nano-7.2/src/text.c:2716: alloc_fn: Storage is returned from allocation function "nmalloc".
nano-7.2/src/text.c:2716: var_assign: Assigning: "curlint" = storage returned from "nmalloc(48UL)".
nano-7.2/src/text.c:2727: var_assign: Assigning: "lints" = "curlint".
nano-7.2/src/text.c:2745: leaked_storage: Variable "curlint" going out of scope leaks the storage it points to.
nano-7.2/src/text.c:2745: leaked_storage: Variable "lints" going out of scope leaks the storage it points to.
# 2743|   	if (!WIFEXITED(lint_status) || WEXITSTATUS(lint_status) > 2) {
# 2744|   		statusline(ALERT, _("Error invoking ''%s''"), openfile->syntax->linter);
# 2745|-> 		return;
# 2746|   	}
# 2747|', false, 'Non-Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-36769

Note to self: Relevant memory free procedure is at the and of this function.', 'Memory allocated for `curlint`/`lints` at line 2716 is not leaked at line 2745, as its scope extends beyond this point and is explicitly freed in the loop starting at line 2896, ensuring proper deallocation regardless of the early return.', '2025-11-18 16:18:56.152762');
INSERT INTO public.ground_truth VALUES (325, 'ncurses-6.4-12.20240127.el10', 1, 'Error: INTEGER_OVERFLOW (CWE-190):
ncurses-6.4-20240127/ncurses/base/lib_set_term.c:390: tainted_data_argument: The value returned in "scolumns" is considered tainted.
ncurses-6.4-20240127/ncurses/base/lib_set_term.c:435: cast_overflow: Truncation due to cast operation on "scolumns" from 32 to 16 bits.
ncurses-6.4-20240127/ncurses/base/lib_set_term.c:435: overflow_assign: "sp->_columns" is assigned from "(short)scolumns".
ncurses-6.4-20240127/ncurses/base/lib_set_term.c:664: overflow_sink: "sp->_columns", which might have overflowed, is passed to "newwin_sp(sp, slines, scolumns, 0, 0)".
#  662|   
#  663|       T(("creating newscr"));
#  664|->     NewScreen(sp) = NCURSES_SP_NAME(newwin) (NCURSES_SP_ARGx slines, scolumns,
#  665|                                                0, 0);
#  666|       if (NewScreen(sp) == 0) {', true, 'Non-Issue', 'dimension_limit() called by _nc_makenew() called by newwin() checks if the dimension is valid (signed 16-bit integer in out (default) build configuration). The screen is not created if it the dimensions didn''t fit into the shorter type.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.156909');
INSERT INTO public.ground_truth VALUES (326, 'ncurses-6.4-12.20240127.el10', 2, 'Error: COMPILER_WARNING:
ncurses-6.4-20240127/ncurses/tinfo/write_entry.c: scope_hint: In function ‘make_db_root’
ncurses-6.4-20240127/ncurses/tinfo/write_entry.c:157:52: warning[-Wformat-overflow=]: ‘%s’ directive writing up to 4090 bytes into a region of size between 5 and 4095
#  157 |             _nc_SPRINTF(dst, _nc_SLIMIT(limit) "%s/%s", top, src);
#      |                                                    ^~
/usr/include/bits/stdio2.h:30:10: note: ‘__sprintf_chk’ output between 2 and 8182 bytes into a destination of size 4096
#   30 |   return __builtin___sprintf_chk (__s, __USE_FORTIFY_LEVEL - 1,
#      |          ^~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
#   31 |                                   __glibc_objsize (__s), __fmt,
#      |                                   ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
#   32 |                                   __va_arg_pack ());
#      |                                   ~~~~~~~~~~~~~~~~~
#  155|       } else {
#  156|   	if ((strlen(top) + strlen(src) + 6) <= limit) {
#  157|-> 	    _nc_SPRINTF(dst, _nc_SLIMIT(limit) "%s/%s", top, src);
#  158|   	    rc = 0;
#  159|   	}', true, 'Issue', 'limit is equal to size of dst from the caller (PATH_MAX). The snprintf call is made only if the output length is below the limit.', 'The code at line 157 is potentially vulnerable to format overflow due to insufficient length checking (line 156) and the possibility of _nc_SPRINTF exceeding the specified limit, as hinted by the -Wformat-overflow warning and the __builtin___sprintf_chk note in stdio2.h.', '2025-11-18 16:18:56.156909');
INSERT INTO public.ground_truth VALUES (327, 'ncurses-6.4-12.20240127.el10', 3, 'Error: INTEGER_OVERFLOW (CWE-190):
ncurses-6.4-20240127/ncurses/base/lib_set_term.c:390: tainted_data_argument: The value returned in "scolumns" is considered tainted.
ncurses-6.4-20240127/ncurses/base/lib_set_term.c:444: overflow: The tainted expression "scolumns" is used in an arithmetic operation. The expression "6 + scolumns" is considered to have possibly overflowed.
ncurses-6.4-20240127/ncurses/base/lib_set_term.c:444: overflow: The expression "(2 + slines) * (6 + scolumns)" is deemed overflowed because at least one of its arguments has overflowed.
ncurses-6.4-20240127/ncurses/base/lib_set_term.c:444: cast_overflow: An assign that casts to a different type, which might trigger an overflow.
ncurses-6.4-20240127/ncurses/base/lib_set_term.c:445: overflow_sink: "sp->out_limit", which might have underflowed, is passed to "malloc(sp->out_limit)".
#  443|   #endif
#  444|       sp->out_limit = (size_t) ((2 + slines) * (6 + scolumns));
#  445|->     if ((sp->out_buffer = malloc(sp->out_limit)) == 0)
#  446|   	sp->out_limit = 0;
#  447|       sp->out_inuse = 0;', true, 'Non-Issue', 'The out buffer can be allocated with an incorrectly calculated value (undefined behavior), but there is no overflow in the buffer. It doesn''t matter how large the buffer really is. Also, in our (default) build configuration using 16-bit integers the function will return error after calling newwin() -> _nc_makenew() -> dimension_limit() as the maximum accepted lines and columns is 32767, which don''t cause overflow in the buffer length calculation.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.156909');
INSERT INTO public.ground_truth VALUES (328, 'ncurses-6.4-12.20240127.el10', 4, 'Error: INTEGER_OVERFLOW (CWE-190):
ncurses-6.4-20240127/ncurses/base/lib_mouse.c:1110: tainted_data_return: Called function "read(sp->_ifd, kbuf + grabbed, (size_t)(3 - (int)grabbed))", and a possible return value may be less than zero.
ncurses-6.4-20240127/ncurses/base/lib_mouse.c:1110: cast_overflow: An assign that casts to a different type, which might trigger an overflow.
ncurses-6.4-20240127/ncurses/base/lib_mouse.c:1107: overflow: The expression "grabbed += (size_t)res" might be negative, but is used in a context that treats it as unsigned.
ncurses-6.4-20240127/ncurses/base/lib_mouse.c:1110: overflow: The expression "3 - (int)grabbed" is deemed underflowed because at least one of its arguments has underflowed.
ncurses-6.4-20240127/ncurses/base/lib_mouse.c:1110: overflow_sink: "(size_t)(3 - (int)grabbed)", which might have underflowed, is passed to "read(sp->_ifd, kbuf + grabbed, (size_t)(3 - (int)grabbed))". [Note: The source code implementation of the function has been overridden by a builtin model.]
# 1108|   
# 1109|   	/* For VIO mouse we add extra bit 64 to disambiguate button-up. */
# 1110|-> 	res = (int) read(
# 1111|   #if USE_EMX_MOUSE
# 1112|   			    (M_FD(sp) >= 0) ? M_FD(sp) : sp->_ifd,', true, 'Non-Issue', 'After the read() call there is a check for -1 and the loop breaks. The variable grabbed cannot be negative.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.156909');
INSERT INTO public.ground_truth VALUES (329, 'rpcbind-1.2.6-4.rc2.el10.4', 1, 'Error: UNINIT (CWE-457):
rpcbind-1.2.6/src/rpcb_svc_com.c:1177: var_decl: Declaring variable "reply_msg" without initializer.
rpcbind-1.2.6/src/rpcb_svc_com.c:1254: uninit_use: Using uninitialized value "reply_msg.rm_xid".
# 1252|   		free(buffer);
# 1253|   
# 1254|-> 	if (reply_msg.rm_xid == 0) {
# 1255|   #ifdef	SVC_RUN_DEBUG
# 1256|   	if (debugging) {', true, 'Non-Issue', 'Initalized in xdr_replymsg()', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.160271');
INSERT INTO public.ground_truth VALUES (330, 'rpcbind-1.2.6-4.rc2.el10.4', 2, 'Error: STRING_SIZE (CWE-120):
rpcbind-1.2.6/src/rpcbind.c:149: string_size_argv: "argv" contains strings with unknown size.
rpcbind-1.2.6/src/rpcbind.c:204: string_size: Passing string "argv[0]" of unknown size to "syslog".
#  202|   		nconf = getnetconfigent("unix");
#  203|   	if (nconf == NULL) {
#  204|-> 		syslog(LOG_ERR, "%s: can''t find local transport\n", argv[0]);
#  205|   		exit(1);
#  206|   	}', true, 'Non-Issue', 'The sizes of argv strings is controled by the shell', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.160271');
INSERT INTO public.ground_truth VALUES (331, 'rpcbind-1.2.6-4.rc2.el10.4', 3, 'Error: RESOURCE_LEAK (CWE-772):
rpcbind-1.2.6/src/rpcinfo.c:947: alloc_fn: Storage is returned from allocation function "malloc".
rpcbind-1.2.6/src/rpcinfo.c:947: var_assign: Assigning: "rs" = storage returned from "malloc(40UL)".
rpcbind-1.2.6/src/rpcinfo.c:954: var_assign: Assigning: "rs_head" = "rs".
rpcbind-1.2.6/src/rpcinfo.c:955: var_assign: Assigning: "rs_tail" = "rs".
rpcbind-1.2.6/src/rpcinfo.c:967: noescape: Resource "rs" is not freed or pointed-to in "add_version".
rpcbind-1.2.6/src/rpcinfo.c:969: noescape: Resource "rs" is not freed or pointed-to in "add_netid".
rpcbind-1.2.6/src/rpcinfo.c:942: var_assign: Assigning: "rs" = "rs_head".
rpcbind-1.2.6/src/rpcinfo.c:967: noescape: Resource "rs" is not freed or pointed-to in "add_version".
rpcbind-1.2.6/src/rpcinfo.c:1017: leaked_storage: Variable "rs_head" going out of scope leaks the storage it points to.
rpcbind-1.2.6/src/rpcinfo.c:1017: leaked_storage: Variable "rs_tail" going out of scope leaks the storage it points to.
rpcbind-1.2.6/src/rpcinfo.c:1017: leaked_storage: Variable "rs" going out of scope leaks the storage it points to.
# 1015|     return;
# 1016|   error:fprintf (stderr, "rpcinfo: no memory\n");
# 1017|->   return;
# 1018|   }
# 1019|', true, 'Non-Issue', 'rpcinfo is a command so the memory is freed when it exits. ', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.160271');
INSERT INTO public.ground_truth VALUES (459, 'texinfo-7.1-2.el10', 14, 'Error: CPPCHECK_WARNING (CWE-404):
texinfo-7.1/install-info/install-info.c:755: error[resourceLeak]: Resource leak: f
#  753|                 nread = fread (data, sizeof (data), 1, f);
#  754|                 if (nread == 0)
#  755|->                 return 0;
#  756|                 goto determine_file_type; /* success */
#  757|               }', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-43595

', NULL, '2025-11-18 16:18:56.203498');
INSERT INTO public.ground_truth VALUES (334, 'sqlite-3.45.1-2.el10', 3, 'Error: BAD_FREE (CWE-590):
sqlite-src-3450100/sqlite3_analyzer.c:150489: address_free: "sqlite3_free_table" frees address of "res.azResult[1]".
sqlite-src-3450100/sqlite3_analyzer.c:150502: address_free: "sqlite3_free_table" frees address of "res.azResult[1]".
sqlite-src-3450100/sqlite3_analyzer.c:150509: address_free: "sqlite3_free_table" frees address of "res.azResult[1]".
#150487|     res.azResult[0] = SQLITE_INT_TO_PTR(res.nData);
#150488|     if( (rc&0xff)==SQLITE_ABORT ){
#150489|->     sqlite3_free_table(&res.azResult[1]);
#150490|       if( res.zErrMsg ){
#150491|         if( pzErrMsg ){', true, 'Non-Issue', 'res.azResult is preoperly allocated by malloc in line n.140 (src/table.c)', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (335, 'sqlite-3.45.1-2.el10', 4, 'Error: OVERRUN (CWE-119):
sqlite-src-3450100/sqlite3_analyzer.c:78553: cond_at_most: Checking "k > 5" implies that "k" may be up to 5 on the false branch.
sqlite-src-3450100/sqlite3_analyzer.c:78548: cond_between: Checking "i < k" implies that "i" is between 2 and 4 (inclusive) on the true branch.
sqlite-src-3450100/sqlite3_analyzer.c:78566: overrun-local: Overrunning array "szNew" of 5 4-byte elements at element index 5 (byte offset 23) using index "i + 1" (which evaluates to 5).
#78564|           }
#78565|         }
#78566|->       szNew[i+1] += sz;
#78567|         cntNew[i]--;
#78568|       }', true, 'Non-Issue', 'This is this check: 
 8388       if( i+1>=k ){
 8389         k = i+2;
 8390         if( k>NB+2 ){ rc = SQLITE_CORRUPT_BKPT; goto balance_cleanup; }
 8391         szNew[k-1] = 0;
 8392         cntNew[k-1] = b.nCell;
 8393       }
that send program to balance cleanup if k>5. And if k==5 on line 8390, then based od 8389 i=3.
That means accessing szNew[i+1] is valid.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (336, 'sqlite-3.45.1-2.el10', 5, 'Error: OVERLAPPING_COPY:
sqlite-src-3450100/sqlite3.c:95886: assign: Assigning: "pIn1" = "&aMem[pOp->p1]".
sqlite-src-3450100/sqlite3.c:95904: equal: The address of "pIn1->u.r" is equal to "aMem + pOp->p1".
sqlite-src-3450100/sqlite3.c:95904: equal: The address of "pIn1->u.i" is equal to "aMem + pOp->p1".
sqlite-src-3450100/sqlite3.c:95904: overlapping_assignment: Assigning "pIn1->u.i" to "pIn1->u.r", which have overlapping memory locations and different types.
sqlite-src-3450100/sqlite3.c:95904: target_type: "pIn1->u.r" has type "double".
sqlite-src-3450100/sqlite3.c:95904: source_type: "pIn1->u.i" has type "long long".
#95902|           pIn1->flags &= ~MEM_Int;
#95903|         }else{
#95904|->         pIn1->u.r = (double)pIn1->u.i;
#95905|           pIn1->flags |= MEM_Real;
#95906|           pIn1->flags &= ~(MEM_Int|MEM_Str);', true, 'Issue', 'pIn1->u.i and pIn1->u.r are both members of the same union in Mem struct (Mem is typedef for sqlite3_value).
225 struct sqlite3_value {
226   union MemValue {
227     double r;           /* Real value used when MEM_Real is set in flags */
228     i64 i;              /* Integer value used when MEM_Int is set in flags */
229     int nZero;          /* Extra zero bytes when MEM_Zero and MEM_Blob set */
230     const char *zPType; /* Pointer type when MEM_Term|MEM_Subtype|MEM_Null */
231     FuncDef *pDef;      /* Used only when flags==MEM_Agg */
232   } u; 
This assingment is in COLTYPE_REAL case, setting flag MEM_Real, which means we need to have pIn1->u.r filled. ', 'Overlapping assignment of different types (`double` and `long long`) at `sqlite-src-3450100/sqlite3.c:95904` occurs, despite explicit casting, with no evidence in the provided code snippet to suggest safe management of this memory overlap, aligning with the CVE''s described vulnerability.', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (337, 'sqlite-3.45.1-2.el10', 6, 'Error: USE_AFTER_FREE (CWE-416):
sqlite-src-3450100/sqlite3_analyzer.c:248896: freed_arg: "fts5FreeCursorComponents" frees "pCsr->pExpr".
sqlite-src-3450100/sqlite3_analyzer.c:249025: deref_arg: Calling "fts5CursorFirst" dereferences freed pointer "pCsr->pExpr".
#249023|         }else{
#249024|           pCsr->ePlan = FTS5_PLAN_MATCH;
#249025|->         rc = fts5CursorFirst(pTab, pCsr, bDesc);
#249026|         }
#249027|       }', false, 'Non-Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-40412

if everything goes allright, then pExpr is allocated properly, however if *zText fails to allocate, then this is a problem. We can avoid this by setting rc to some error in the beginning.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (338, 'sqlite-3.45.1-2.el10', 7, 'Error: USE_AFTER_FREE (CWE-416):
sqlite-src-3450100/sqlite3.c:87891: freed_arg: "sqlite3VdbeTransferError" frees "p->zErrMsg".
sqlite-src-3450100/sqlite3.c:87908: pass_freed_arg: Passing freed pointer "p->zErrMsg" as an argument to "sqlite3DbFree".
#87906|   #endif
#87907|     if( p->zErrMsg ){
#87908|->     sqlite3DbFree(db, p->zErrMsg);
#87909|       p->zErrMsg = 0;
#87910|     }', true, 'Non-Issue', 'From sqlite''s comments:
Calling sqlite3DbFree(D,X) for X==0 is a harmless no-op.

sqlite3DbFree contains null pointer check.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (460, 'texinfo-7.1-2.el10', 15, 'Error: OVERRUN (CWE-119):
texinfo-7.1/info/session.c:3483: alloc_strlen: Allocating insufficient memory for the terminating null of the string. [Note: The source code implementation of the function has been overridden by a builtin model.]
# 3481|             char *nodename;
# 3482|   
# 3483|->           nodename = xmalloc (strlen (program) + strlen (*try_node));
# 3484|             sprintf (nodename, *try_node, program);
# 3485|             /* The last resort "%s" is dangerous, so we restrict it', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-43595

', NULL, '2025-11-18 16:18:56.203498');
INSERT INTO public.ground_truth VALUES (339, 'sqlite-3.45.1-2.el10', 8, 'Error: INTEGER_OVERFLOW (CWE-190):
sqlite-src-3450100/sqlite3_analyzer.c:201059: cast_overflow: Truncation due to cast operation on "pPhrase->iHead" from 64 to 32 bits.
sqlite-src-3450100/sqlite3_analyzer.c:201059: overflow_assign: "iEnd" is assigned from "pPhrase->iHead".
sqlite-src-3450100/sqlite3_analyzer.c:201066: overflow: The expression "iEnd - pIter->nSnippet" is deemed overflowed because at least one of its arguments has overflowed.
sqlite-src-3450100/sqlite3_analyzer.c:201066: overflow: The expression "iEnd - pIter->nSnippet + 1" is deemed underflowed because at least one of its arguments has underflowed.
sqlite-src-3450100/sqlite3_analyzer.c:201066: assign: Assigning: "iStart" = "iEnd - pIter->nSnippet + 1".
sqlite-src-3450100/sqlite3_analyzer.c:201070: overflow_sink: "iStart", which might have underflowed, is passed to "fts3SnippetAdvance(&pPhrase->pTail, &pPhrase->iTail, iStart)".
#201068|         SnippetPhrase *pPhrase = &pIter->aPhrase[i];
#201069|         fts3SnippetAdvance(&pPhrase->pHead, &pPhrase->iHead, iEnd+1);
#201070|->       fts3SnippetAdvance(&pPhrase->pTail, &pPhrase->iTail, iStart);
#201071|       }
#201072|     }', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-40412

seems like underflow is possible here', 'Explicit 64-to-32-bit cast overflow on `pPhrase->iHead` (line #201069) propagates through arithmetic operations, causing potential overflows (`#201066`) and underflows (`#201066`), ultimately reaching a vulnerable sink (`fts3SnippetAdvance` at `#201070`) without explicit bounds checking.', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (341, 'sqlite-3.45.1-2.el10', 10, 'Error: OVERRUN (CWE-119):
sqlite-src-3450100/sqlite3_analyzer.c:78553: cond_at_most: Checking "k > 5" implies that "k" may be up to 5 on the false branch.
sqlite-src-3450100/sqlite3_analyzer.c:78548: cond_between: Checking "i < k" implies that "i" is between 2 and 4 (inclusive) on the true branch.
sqlite-src-3450100/sqlite3_analyzer.c:78581: overrun-local: Overrunning array "szNew" of 5 4-byte elements at element index 5 (byte offset 23) using index "i + 1" (which evaluates to 5).
#78579|           }
#78580|         }
#78581|->       szNew[i+1] -= sz;
#78582|       }
#78583|       if( cntNew[i]>=b.nCell ){', true, 'Non-Issue', 'The is this check: 
 8388       if( i+1>=k ){
 8389         k = i+2;
 8390         if( k>NB+2 ){ rc = SQLITE_CORRUPT_BKPT; goto balance_cleanup; }
 8391         szNew[k-1] = 0;
 8392         cntNew[k-1] = b.nCell;
 8393       }
that send program to balance cleanup if k>5. And if k==5 on line 8390, then based od 8389 i=3.
That means accessing szNew[i+1] is valid.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (342, 'sqlite-3.45.1-2.el10', 11, 'Error: BAD_FREE (CWE-763):
sqlite-src-3450100/shell.c:15039: array_address: Taking address of array """".
sqlite-src-3450100/shell.c:15039: assign: Assigning: "zSchema" = """".
sqlite-src-3450100/shell.c:15069: incorrect_free: "sqlite3_bind_text" frees incorrect pointer "zSchema".
#15067|     }
#15068|     if( rc==SQLITE_OK ){
#15069|->     rc = sqlite3_bind_text(pCsr->pStmt, 1, zSchema, -1, SQLITE_TRANSIENT);
#15070|     }
#15071|', true, 'Non-Issue', 'setting SQLITE_TRANSIENT disables memory freeing', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (343, 'sqlite-3.45.1-2.el10', 12, 'Error: INTEGER_OVERFLOW (CWE-190):
sqlite-src-3450100/sqlite3_analyzer.c:201059: cast_overflow: Truncation due to cast operation on "pPhrase->iHead" from 64 to 32 bits.
sqlite-src-3450100/sqlite3_analyzer.c:201059: overflow_assign: "iEnd" is assigned from "pPhrase->iHead".
sqlite-src-3450100/sqlite3_analyzer.c:201069: overflow: The expression "iEnd + 1" is deemed overflowed because at least one of its arguments has overflowed.
sqlite-src-3450100/sqlite3_analyzer.c:201069: overflow_sink: "iEnd + 1", which might have underflowed, is passed to "fts3SnippetAdvance(&pPhrase->pHead, &pPhrase->iHead, iEnd + 1)".
#201067|       for(i=0; i<pIter->nPhrase; i++){
#201068|         SnippetPhrase *pPhrase = &pIter->aPhrase[i];
#201069|->       fts3SnippetAdvance(&pPhrase->pHead, &pPhrase->iHead, iEnd+1);
#201070|         fts3SnippetAdvance(&pPhrase->pTail, &pPhrase->iTail, iStart);
#201071|       }', true, 'Non-Issue', 'iCol in this case can not be bigger that i16, as the condition in for loop look looks as follows:
for(iCol=0; iCol<pTab->nCol; iCol++){ and pTab->nCol is i16', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (344, 'sqlite-3.45.1-2.el10', 13, 'Error: USE_AFTER_FREE (CWE-416):
sqlite-src-3450100/sqlite3.c:116587: assign: Assigning: "zBuf1" = "&zOut[nSql * 2LL + 1LL]".
sqlite-src-3450100/sqlite3.c:116588: assign: Assigning: "zBuf2" = "&zOut[nSql * 4LL + 2LL]".
sqlite-src-3450100/sqlite3.c:116627: assign: Assigning: "zReplace" = "zBuf2".
sqlite-src-3450100/sqlite3.c:116643: freed_arg: "sqlite3_result_text" frees "zOut".
sqlite-src-3450100/sqlite3.c:116644: pass_freed_arg: Passing freed pointer "zOut" as an argument to "sqlite3DbFree".
#116642|   
#116643|       sqlite3_result_text(pCtx, zOut, -1, SQLITE_TRANSIENT);
#116644|->     sqlite3DbFree(db, zOut);
#116645|     }else{
#116646|       rc = SQLITE_NOMEM;', true, 'Non-Issue', 'memory is not freed when xDel is set to SQLITE_TRANSIENT, which it is.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (345, 'sqlite-3.45.1-2.el10', 14, 'Error: INTEGER_OVERFLOW (CWE-190):
sqlite-src-3450100/sqlite3_analyzer.c:127646: cast_overflow: Truncation due to cast operation on "iCol" from 32 to 16 bits.
sqlite-src-3450100/sqlite3_analyzer.c:127647: overflow_sink: "iCol", which might have overflowed, is passed to "sqlite3ExprCodeGetColumnOfTable(v, pTab, iDataCur, iCol, iOld + kk + 1)".
#127645|         if( mask==0xffffffff || (iCol<=31 && (mask & MASKBIT32(iCol))!=0) ){
#127646|           int kk = sqlite3TableColumnToStorage(pTab, iCol);
#127647|->         sqlite3ExprCodeGetColumnOfTable(v, pTab, iDataCur, iCol, iOld+kk+1);
#127648|         }
#127649|       }', false, 'Non-Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-40412

', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (346, 'sqlite-3.45.1-2.el10', 15, 'Error: UNINIT (CWE-457):
sqlite-src-3450100/sqlite3.c:204845: skipped_decl: Jumping over declaration of "opcode".
sqlite-src-3450100/sqlite3.c:204899: uninit_use_in_call: Using uninitialized value "opcode" when calling "jsonBlobAppendNode".
#204897|         j++;
#204898|       }
#204899|->     jsonBlobAppendNode(pParse, opcode, j-1-i, &z[i+1]);
#204900|       return j+1;
#204901|     }', true, 'Non-Issue', 'The opcode is correctly initilized to JSONB_TEXT before jumping to the parse_string flag.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (347, 'sqlite-3.45.1-2.el10', 16, 'Error: OVERRUN (CWE-119):
sqlite-src-3450100/sqlite3_analyzer.c:78860: cond_const: Checking "k < 6" implies that "k" is 6 on the false branch.
sqlite-src-3450100/sqlite3_analyzer.c:78861: overrun-local: Overrunning array "b.apEnd" of 6 8-byte elements at element index 6 (byte offset 55) using index "k" (which evaluates to 6).
#78859|       assert( iOvflSpace <= (int)pBt->pageSize );
#78860|       for(k=0; ALWAYS(k<NB*2) && b.ixNx[k]<=j; k++){}
#78861|->     pSrcEnd = b.apEnd[k];
#78862|       if( SQLITE_OVERFLOW(pSrcEnd, pCell, pCell+sz) ){
#78863|         rc = SQLITE_CORRUPT_BKPT;', true, 'Non-Issue', 'As per upstream this is not a bug:
https://sqlite.org/forum/forumpost/d0e144c233c7b286 
Upstream also added some asserts to future releases so that static analysers are not confused.
In my opinion these asserts do not need to be backported.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (348, 'sqlite-3.45.1-2.el10', 17, 'Error: VARARGS (CWE-237):
sqlite-src-3450100/shell.c:15571: va_init: Initializing va_list "ap".
sqlite-src-3450100/shell.c:15579: missing_va_end: "va_end" was not called for "ap".
#15577|     p->zErrMsg = z;
#15578|     p->errCode = errCode;
#15579|->   return errCode;
#15580|   }
#15581|', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-40412

', 'The code initializes ''va_list'' ''ap'' at line 15571 but lacks a corresponding ''va_end'' call, leading to a potential resource leak or undefined behavior, as it returns at line 15579 without cleanup.', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (349, 'sqlite-3.45.1-2.el10', 18, 'Error: USE_AFTER_FREE (CWE-416):
sqlite-src-3450100/shell.c:11204: freed_arg: "sqlite3_result_text" frees "zRes".
sqlite-src-3450100/shell.c:11205: double_free: Calling "sqlite3_free" frees pointer "zRes" which has already been freed. [Note: The source code implementation of the function has been overridden by a builtin model.]
#11203|       }else{
#11204|         sqlite3_result_text(context, zRes, -1, SQLITE_TRANSIENT);
#11205|->       sqlite3_free(zRes);
#11206|       }
#11207|     }', true, 'Non-Issue', 'sqlite3_result_text does not free zRes, when SQLITE_TRANSIENT is set.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (350, 'sqlite-3.45.1-2.el10', 19, 'Error: OVERRUN (CWE-119):
sqlite-src-3450100/sqlite3_analyzer.c:210526: cond_const: Checking "iIdx < 40" implies that "iIdx" is 41 on the false branch.
sqlite-src-3450100/sqlite3_analyzer.c:210585: overrun-buffer-arg: Overrunning array "zIdxStr" of 41 bytes by passing it to a function which accesses it at byte offset 41 using argument "iIdx + 1" (which evaluates to 42). [Note: The source code implementation of the function has been overridden by a builtin model.]
#210583|         return SQLITE_NOMEM;
#210584|       }
#210585|->     memcpy(pIdxInfo->idxStr, zIdxStr, iIdx+1);
#210586|     }
#210587|', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-40412

', 'iIdx is implied to be 41 when `iIdx < 40` is false, and using `iIdx+1` (42) as the length in `memcpy` at line 210585 would overrun the 41-byte `zIdxStr` array, with no intervening code modifications to prevent this.', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (351, 'sqlite-3.45.1-2.el10', 20, 'Error: USE_AFTER_FREE (CWE-416):
sqlite-src-3450100/sqlite3.c:67013: freed_arg: "sqlite3WalCheckpoint" frees "pWal->apWiData".
sqlite-src-3450100/sqlite3.c:67046: double_free: Calling "sqlite3_free" frees pointer "pWal->apWiData" which has already been freed. [Note: The source code implementation of the function has been overridden by a builtin model.]
#67044|       }
#67045|       WALTRACE(("WAL%p: closed\n", pWal));
#67046|->     sqlite3_free((void *)pWal->apWiData);
#67047|       sqlite3_free(pWal);
#67048|     }', true, 'Non-Issue', 'Its calling realloc, not free.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (352, 'sqlite-3.45.1-2.el10', 21, 'Error: USE_AFTER_FREE (CWE-416):
sqlite-src-3450100/sqlite3.c:248872: freed_arg: "fts5FreeCursorComponents" frees "pCsr->pExpr".
sqlite-src-3450100/sqlite3.c:249001: deref_arg: Calling "fts5CursorFirst" dereferences freed pointer "pCsr->pExpr".
#248999|         }else{
#249000|           pCsr->ePlan = FTS5_PLAN_MATCH;
#249001|->         rc = fts5CursorFirst(pTab, pCsr, bDesc);
#249002|         }
#249003|       }', true, 'Non-Issue', 'There is a check, that check if pCsr->pExpr is not NULL.
Only when it is not NULL this is executed.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (353, 'sqlite-3.45.1-2.el10', 22, 'Error: USE_AFTER_FREE (CWE-416):
sqlite-src-3450100/sqlite3.c:191091: freed_arg: "sqlite3_free" frees "pCsr->filter.zTerm". [Note: The source code implementation of the function has been overridden by a builtin model.]
sqlite-src-3450100/sqlite3.c:191127: pass_freed_arg: Passing freed pointer "pCsr->filter.zTerm" as an argument to "sqlite3Fts3SegReaderCursor".
#191125|     pCsr->iLangid = iLangVal;
#191126|   
#191127|->   rc = sqlite3Fts3SegReaderCursor(pFts3, iLangVal, 0, FTS3_SEGCURSOR_ALL,
#191128|         pCsr->filter.zTerm, pCsr->filter.nTerm, 0, isScan, &pCsr->csr
#191129|     );', true, 'Non-Issue', 'pCsr.filter is set to NULL and nTerm is set to zero to avoid invalid access.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (354, 'sqlite-3.45.1-2.el10', 23, 'Error: OVERRUN (CWE-119):
sqlite-src-3450100/sqlite3.c:78836: cond_const: Checking "k < 6" implies that "k" is 6 on the false branch.
sqlite-src-3450100/sqlite3.c:78837: overrun-local: Overrunning array "b.apEnd" of 6 8-byte elements at element index 6 (byte offset 55) using index "k" (which evaluates to 6).
#78835|       assert( iOvflSpace <= (int)pBt->pageSize );
#78836|       for(k=0; ALWAYS(k<NB*2) && b.ixNx[k]<=j; k++){}
#78837|->     pSrcEnd = b.apEnd[k];
#78838|       if( SQLITE_OVERFLOW(pSrcEnd, pCell, pCell+sz) ){
#78839|         rc = SQLITE_CORRUPT_BKPT;', true, 'Non-Issue', 'As per upstream this is not a bug:
https://sqlite.org/forum/forumpost/d0e144c233c7b286 
Upstream also added some asserts to future releases so that static analysers are not confused.
In my opinion these asserts do not need to be backported.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (355, 'sqlite-3.45.1-2.el10', 24, 'Error: USE_AFTER_FREE (CWE-416):
sqlite-src-3450100/sqlite3_analyzer.c:82578: alias: Equality between "pMem->z" and "pMem->zMalloc" implies that they are aliases.
sqlite-src-3450100/sqlite3_analyzer.c:82582: freed_arg: "sqlite3Realloc" frees "pMem->z".
sqlite-src-3450100/sqlite3_analyzer.c:82583: double_free: Calling "sqlite3_free" frees pointer "pMem->z" which has already been freed. [Note: The source code implementation of the function has been overridden by a builtin model.]
#82581|       }else{
#82582|         pMem->zMalloc = sqlite3Realloc(pMem->z, n);
#82583|->       if( pMem->zMalloc==0 ) sqlite3_free(pMem->z);
#82584|         pMem->z = pMem->zMalloc;
#82585|       }', true, 'Non-Issue', 'pMem->z is freed only when something during realloc goes wrong, which is valid.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (461, 'texinfo-7.1-2.el10', 16, 'Error: CPPCHECK_WARNING (CWE-404):
texinfo-7.1/install-info/install-info.c:857: error[resourceLeak]: Resource leak: f
#  855|         /* Seek back over the magic bytes.  */
#  856|         if (fseek (f, 0, 0) < 0)
#  857|->         return 0;
#  858|   #endif
#  859|       }', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-43595

', NULL, '2025-11-18 16:18:56.203498');
INSERT INTO public.ground_truth VALUES (356, 'sqlite-3.45.1-2.el10', 25, 'Error: OVERRUN (CWE-119):
sqlite-src-3450100/sqlite3_analyzer.c:186302: overrun-local: Overrunning array of 8 bytes at byte offset 8 by dereferencing pointer "&&pCsr->base[1]". [Note: The source code implementation of the function has been overridden by a builtin model.]
#186300|     sqlite3Fts3MIBufferFree(pCsr->pMIBuffer);
#186301|     sqlite3Fts3ExprFree(pCsr->pExpr);
#186302|->   memset(&(&pCsr->base)[1], 0, sizeof(Fts3Cursor)-sizeof(sqlite3_vtab_cursor));
#186303|   }
#186304|', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-40412

This can be problem on weird processor architecture
use either offset of, or start memset from second member of Fts3Cursor
offsetoff solution is segfaulting atm
diff --git a/ext/fts3/fts3.c b/ext/fts3/fts3.c
index f977aabfbc..c90bd382c9 100644
--- a/ext/fts3/fts3.c
+++ b/ext/fts3/fts3.c
@@ -1785,7 +1785,8 @@ static void fts3ClearCursor(Fts3Cursor *pCsr){
   sqlite3_free(pCsr->aDoclist);
   sqlite3Fts3MIBufferFree(pCsr->pMIBuffer);
   sqlite3Fts3ExprFree(pCsr->pExpr);
-  memset(&(&pCsr->base)[1], 0, sizeof(Fts3Cursor)-sizeof(sqlite3_vtab_cursor));
+        size_t offset = offsetof(struct Fts3Cursor, base);
+  memset((char*)pCsr+offset, 0, sizeof(Fts3Cursor)-offset);
 }

 /*', 'Insufficient evidence to rule out buffer overrun due to uncertain structure layout and missing definitions for `Fts3Cursor` and `sqlite3_vtab_cursor`, despite syntactically valid `memset` operation at line #186302.', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (357, 'sqlite-3.45.1-2.el10', 26, 'Error: USE_AFTER_FREE (CWE-416):
sqlite-src-3450100/shell.c:16035: freed_arg: "sqlite3_result_text" frees "zOut".
sqlite-src-3450100/shell.c:16036: double_free: Calling "sqlite3_free" frees pointer "zOut" which has already been freed. [Note: The source code implementation of the function has been overridden by a builtin model.]
#16034|   
#16035|         sqlite3_result_text(context, zOut, iOut, SQLITE_TRANSIENT);
#16036|->       sqlite3_free(zOut);
#16037|         return;
#16038|       }', true, 'Non-Issue', 'when the SQLITE_TRANSIENT flag is set, the  array is not freed.
That is also this case.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (358, 'sqlite-3.45.1-2.el10', 27, 'Error: BAD_FREE (CWE-590):
sqlite-src-3450100/sqlite3_analyzer.c:250581: array_free: "sqlite3_result_text" frees array ""fts5: 2024-01-30 16:01:20 e876e51a0ed5c5b3126f52e532044363a014bc594cfefa87ffb5b82257cc467a"".
#250579|     assert( nArg==0 );
#250580|     UNUSED_PARAM2(nArg, apUnused);
#250581|->   sqlite3_result_text(pCtx, "fts5: 2024-01-30 16:01:20 e876e51a0ed5c5b3126f52e532044363a014bc594cfefa87ffb5b82257cc467a", -1, SQLITE_TRANSIENT);
#250582|   }
#250583|', true, 'Non-Issue', 'when the SQLITE_TRANSIENT flag is set, the array is not freed. That is also this case.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (359, 'sqlite-3.45.1-2.el10', 28, 'Error: OVERRUN (CWE-119):
sqlite-src-3450100/sqlite3_analyzer.c:77666: cond_at_most: Checking "k < 6" implies that "k" may be up to 5 on the true branch.
sqlite-src-3450100/sqlite3_analyzer.c:77692: incr: Incrementing "k". The value of "k" may now be up to 6.
sqlite-src-3450100/sqlite3_analyzer.c:77693: overrun-local: Overrunning array "pCArray->apEnd" of 6 8-byte elements at element index 6 (byte offset 55) using index "k" (which evaluates to 6).
#77691|       if( pCArray->ixNx[k]<=i ){
#77692|         k++;
#77693|->       pSrcEnd = pCArray->apEnd[k];
#77694|       }
#77695|     }', true, 'Non-Issue', 'As per upstream this is not a bug: https://sqlite.org/forum/forumpost/d0e144c233c7b286 Upstream also added some asserts to future releases so that static analysers are not confused. In my opinion these asserts do not need to be backported.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (360, 'sqlite-3.45.1-2.el10', 29, 'Error: OVERRUN (CWE-119):
sqlite-src-3450100/sqlite3.c:78529: cond_at_most: Checking "k > 5" implies that "k" may be up to 5 on the false branch.
sqlite-src-3450100/sqlite3.c:78524: cond_between: Checking "i < k" implies that "i" is between 2 and 4 (inclusive) on the true branch.
sqlite-src-3450100/sqlite3.c:78557: overrun-local: Overrunning array "szNew" of 5 4-byte elements at element index 5 (byte offset 23) using index "i + 1" (which evaluates to 5).
#78555|           }
#78556|         }
#78557|->       szNew[i+1] -= sz;
#78558|       }
#78559|       if( cntNew[i]>=b.nCell ){', true, 'Non-Issue', 'The is this check: 
 8388       if( i+1>=k ){
 8389         k = i+2;
 8390         if( k>NB+2 ){ rc = SQLITE_CORRUPT_BKPT; goto balance_cleanup; }
 8391         szNew[k-1] = 0;
 8392         cntNew[k-1] = b.nCell;
 8393       }
that send program to balance cleanup if k>5. And if k==5 on line 8390, then based od 8389 i=3.
That means accessing szNew[i+1] is valid.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (361, 'sqlite-3.45.1-2.el10', 30, 'Error: OVERRUN (CWE-119):
sqlite-src-3450100/sqlite3.c:35643: assignment: Assigning: "i" = "23".
sqlite-src-3450100/sqlite3.c:35653: overrun-local: Overrunning array "p->zBuf" of 24 bytes at byte offset 24 using index "i + 1" (which evaluates to 24).
#35651|     if( iRound<0 ){
#35652|       iRound = p->iDP - iRound;
#35653|->     if( iRound==0 && p->zBuf[i+1]>=''5'' ){
#35654|         iRound = 1;
#35655|         p->zBuf[i--] = ''0'';', false, 'Non-Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-40412

', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (362, 'sqlite-3.45.1-2.el10', 31, 'Error: BAD_FREE (CWE-590):
sqlite-src-3450100/sqlite3.c:250557: array_free: "sqlite3_result_text" frees array ""fts5: 2024-01-30 16:01:20 e876e51a0ed5c5b3126f52e532044363a014bc594cfefa87ffb5b82257cc467a"".
#250555|     assert( nArg==0 );
#250556|     UNUSED_PARAM2(nArg, apUnused);
#250557|->   sqlite3_result_text(pCtx, "fts5: 2024-01-30 16:01:20 e876e51a0ed5c5b3126f52e532044363a014bc594cfefa87ffb5b82257cc467a", -1, SQLITE_TRANSIENT);
#250558|   }
#250559|', true, 'Non-Issue', 'when the SQLITE_TRANSIENT flag is set, the  array is not freed.
That is also this case.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (363, 'sqlite-3.45.1-2.el10', 32, 'Error: OVERRUN (CWE-119):
sqlite-src-3450100/sqlite3.c:186439: buffer_alloc: Calling allocating function "sqlite3_realloc64" which allocates "nAlloc" bytes. [Note: The source code implementation of the function has been overridden by a builtin model.]
sqlite-src-3450100/sqlite3.c:186439: var_assign: Assigning: "zNew" = "sqlite3_realloc64(zBuffer, nAlloc)".
sqlite-src-3450100/sqlite3.c:186444: alias: Assigning: "zBuffer" = "zNew".
sqlite-src-3450100/sqlite3.c:186436: symbolic_compare: Tracking "(i64)nPrefix + nSuffix" since "nAlloc" is tracked with "zNew".
sqlite-src-3450100/sqlite3.c:186436: symbolic_compare: Tracking "(i64)nPrefix + nSuffix" since "nAlloc" is tracked with "zBuffer".
sqlite-src-3450100/sqlite3.c:186448: symbolic_assign: Tracking "nBuffer" since "nPrefix + nSuffix" is tracked with "zNew".
sqlite-src-3450100/sqlite3.c:186448: symbolic_assign: Tracking "nBuffer" since "nPrefix + nSuffix" is tracked with "zBuffer".
sqlite-src-3450100/sqlite3.c:186423: symbolic_compare: Tracking "nPrefix" since "nBuffer" is tracked with "zNew".
sqlite-src-3450100/sqlite3.c:186423: symbolic_compare: Tracking "nPrefix" since "nBuffer" is tracked with "zBuffer".
sqlite-src-3450100/sqlite3.c:186432: symbolic_compare: Tracking "zCsr - zNode" since "nPrefix" is tracked with "zNew".
sqlite-src-3450100/sqlite3.c:186432: symbolic_compare: Tracking "nPrefix" since "zCsr - zNode" is tracked with "zBuffer".
sqlite-src-3450100/sqlite3.c:186436: symbolic_compare: Tracking "(i64)nPrefix + nSuffix" since "nAlloc" is tracked with "zNew".
sqlite-src-3450100/sqlite3.c:186436: symbolic_compare: Tracking "(i64)nPrefix + nSuffix" since "nAlloc" is tracked with "zBuffer".
sqlite-src-3450100/sqlite3.c:186447: overrun-local: Overrunning dynamic array "zBuffer" at offset corresponding to index variable "nPrefix".
#186445|       }
#186446|       assert( zBuffer );
#186447|->     memcpy(&zBuffer[nPrefix], zCsr, nSuffix);
#186448|       nBuffer = nPrefix + nSuffix;
#186449|       zCsr += nSuffix;', true, 'Non-Issue', 'nBuffer is realloc -ed when nPrefix is bigger.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (365, 'sqlite-3.45.1-2.el10', 34, 'Error: COPY_PASTE_ERROR (CWE-398):
sqlite-src-3450100/sqlite3_analyzer.c:170152: original: "pMWin->regStartRowid" looks like the original copy.
sqlite-src-3450100/sqlite3_analyzer.c:170164: copy_paste_error: "regStartRowid" in "pMWin->regStartRowid" looks like a copy-paste error.
sqlite-src-3450100/sqlite3_analyzer.c:170164: remediation: Should it say "regEndRowid" instead?
#170162|         csr = p->end.csr;
#170163|         reg = p->end.reg;
#170164|->       if( pMWin->regStartRowid ){
#170165|           assert( pMWin->regEndRowid );
#170166|           sqlite3VdbeAddOp2(v, OP_AddImm, pMWin->regEndRowid, 1);', true, 'Non-Issue', 'there is also assert for checking in regEndRowid is not NULL. That is sufficient.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (366, 'sqlite-3.45.1-2.el10', 35, 'Error: OVERRUN (CWE-119):
sqlite-src-3450100/sqlite3_analyzer.c:77666: cond_at_most: Checking "k < 6" implies that "k" may be up to 5 on the true branch.
sqlite-src-3450100/sqlite3_analyzer.c:77692: incr: Incrementing "k". The value of "k" may now be up to 6.
sqlite-src-3450100/sqlite3_analyzer.c:77691: overrun-local: Overrunning array "pCArray->ixNx" of 6 4-byte elements at element index 6 (byte offset 27) using index "k" (which evaluates to 6).
#77689|       i++;
#77690|       if( i>=iEnd ) break;
#77691|->     if( pCArray->ixNx[k]<=i ){
#77692|         k++;
#77693|         pSrcEnd = pCArray->apEnd[k];', true, 'Non-Issue', 'As per upstream this is not a bug:
https://sqlite.org/forum/forumpost/d0e144c233c7b286 
Upstream also added some asserts to future releases so that static analysers are not confused.
In my opinion these asserts do not need to be backported.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (367, 'sqlite-3.45.1-2.el10', 36, 'Error: OVERRUN (CWE-119):
sqlite-src-3450100/sqlite3.c:77725: cond_const: Checking "k < 6" implies that "k" is 6 on the false branch.
sqlite-src-3450100/sqlite3.c:77726: overrun-local: Overrunning array "pCArray->apEnd" of 6 8-byte elements at element index 6 (byte offset 55) using index "k" (which evaluates to 6).
#77724|     if( iEnd<=iFirst ) return 0;
#77725|     for(k=0; ALWAYS(k<NB*2) && pCArray->ixNx[k]<=i ; k++){}
#77726|->   pEnd = pCArray->apEnd[k];
#77727|     while( 1 /*Exit by break*/ ){
#77728|       int sz, rc;', true, 'Non-Issue', 'As per upstream this is not a bug:
https://sqlite.org/forum/forumpost/d0e144c233c7b286 
Upstream also added some asserts to future releases so that static analysers are not confused.
In my opinion these asserts do not need to be backported.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (368, 'sqlite-3.45.1-2.el10', 37, 'Error: OVERLAPPING_COPY:
sqlite-src-3450100/sqlite3_analyzer.c:95910: assign: Assigning: "pIn1" = "&aMem[pOp->p1]".
sqlite-src-3450100/sqlite3_analyzer.c:95928: equal: The address of "pIn1->u.r" is equal to "aMem + pOp->p1".
sqlite-src-3450100/sqlite3_analyzer.c:95928: equal: The address of "pIn1->u.i" is equal to "aMem + pOp->p1".
sqlite-src-3450100/sqlite3_analyzer.c:95928: overlapping_assignment: Assigning "pIn1->u.i" to "pIn1->u.r", which have overlapping memory locations and different types.
sqlite-src-3450100/sqlite3_analyzer.c:95928: target_type: "pIn1->u.r" has type "double".
sqlite-src-3450100/sqlite3_analyzer.c:95928: source_type: "pIn1->u.i" has type "long long".
#95926|           pIn1->flags &= ~MEM_Int;
#95927|         }else{
#95928|->         pIn1->u.r = (double)pIn1->u.i;
#95929|           pIn1->flags |= MEM_Real;
#95930|           pIn1->flags &= ~(MEM_Int|MEM_Str);', true, 'Issue', 'pIn1->u.i and pIn1->u.r are both members of the same union in Mem struct (Mem is typedef for sqlite3_value).
225 struct sqlite3_value {
226   union MemValue {
227     double r;           /* Real value used when MEM_Real is set in flags */
228     i64 i;              /* Integer value used when MEM_Int is set in flags */
229     int nZero;          /* Extra zero bytes when MEM_Zero and MEM_Blob set */
230     const char *zPType; /* Pointer type when MEM_Term|MEM_Subtype|MEM_Null */
231     FuncDef *pDef;      /* Used only when flags==MEM_Agg */
232   } u; 
This assingment is in COLTYPE_REAL case, setting flag MEM_Real, which means we need to have pIn1->u.r filled. ', 'Overlapping assignment with type mismatch occurs at line #95928, where a `long long` (`pIn1->u.i`) is cast to a `double` (`pIn1->u.r`) in overlapping memory locations, potentially leading to type confusion or unexpected behavior.', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (369, 'sqlite-3.45.1-2.el10', 38, 'Error: USE_AFTER_FREE (CWE-416):
sqlite-src-3450100/sqlite3.c:237109: freed_arg: "fts5ExprAddChildren" frees "pRight".
sqlite-src-3450100/sqlite3.c:237126: double_free: Calling "sqlite3Fts5ParseNodeFree" frees pointer "pRight" which has already been freed.
#237124|       assert( pParse->rc!=SQLITE_OK );
#237125|       sqlite3Fts5ParseNodeFree(pLeft);
#237126|->     sqlite3Fts5ParseNodeFree(pRight);
#237127|       sqlite3Fts5ParseNearsetFree(pNear);
#237128|     }', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-40412

Fix could be setting freed pointer to null.', 'Consecutive calls to `sqlite3Fts5ParseNodeFree` for `pRight` (lines 237109 and 237126) with no intervening nullification or protective checks, confirming a direct execution path to a double-free vulnerability (CWE-416).', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (370, 'sqlite-3.45.1-2.el10', 39, 'Error: BAD_FREE (CWE-590):
sqlite-src-3450100/shell.c:2057: array_free: "sqlite3_result_text" frees array "z".
# 2055|     if( n>350 ) n = 350;
# 2056|     sqlite3_snprintf(sizeof(z), z, "%#+.*e", n, r);
# 2057|->   sqlite3_result_text(pCtx, z, -1, SQLITE_TRANSIENT);
# 2058|   }
# 2059|', true, 'Non-Issue', 'when the SQLITE_TRANSIENT flag is set, the array is not freed. That is also this case.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (371, 'sqlite-3.45.1-2.el10', 40, 'Error: OVERRUN (CWE-119):
sqlite-src-3450100/sqlite3.c:186278: overrun-local: Overrunning array of 8 bytes at byte offset 8 by dereferencing pointer "&&pCsr->base[1]". [Note: The source code implementation of the function has been overridden by a builtin model.]
#186276|     sqlite3Fts3MIBufferFree(pCsr->pMIBuffer);
#186277|     sqlite3Fts3ExprFree(pCsr->pExpr);
#186278|->   :qCsr->base)[1], 0, sizeof(Fts3Cursor)-sizeof(sqlite3_vtab_cursor));
#186279|   }
#186280|', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-40412

need help
use either offset of, or start memset from second member of Fts3Cursor
offsetoff solution is segfaulting atm
diff --git a/ext/fts3/fts3.c b/ext/fts3/fts3.c
index f977aabfbc..c90bd382c9 100644
--- a/ext/fts3/fts3.c
+++ b/ext/fts3/fts3.c
@@ -1785,7 +1785,8 @@ static void fts3ClearCursor(Fts3Cursor *pCsr){
   sqlite3_free(pCsr->aDoclist);
   sqlite3Fts3MIBufferFree(pCsr->pMIBuffer);
   sqlite3Fts3ExprFree(pCsr->pExpr);
-  memset(&(&pCsr->base)[1], 0, sizeof(Fts3Cursor)-sizeof(sqlite3_vtab_cursor));
+        size_t offset = offsetof(struct Fts3Cursor, base);
+  memset((char*)pCsr+offset, 0, sizeof(Fts3Cursor)-offset);
 }

 /*', 'Explicit dereference of `&&pCsr->base[1]` at byte offset 8 exceeds the 8-byte array size, with no visible bounds checking in the provided code snippet (#186276-#186280), identifying a single vulnerable execution path.', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (372, 'sqlite-3.45.1-2.el10', 41, 'Error: NEGATIVE_RETURNS (CWE-394):
sqlite-src-3450100/shell.c:13351: negative_return_fn: Function "sqlite3_value_int(argv[0])" returns a negative number.
sqlite-src-3450100/shell.c:13351: assign: Assigning: "iSlot" = "sqlite3_value_int(argv[0])".
sqlite-src-3450100/shell.c:13353: negative_returns: Using variable "iSlot" as an index to array "p->aSlot".
#13351|     iSlot = sqlite3_value_int(argv[0]);
#13352|     assert( iSlot<=p->nSlot );
#13353|->   pSlot = &p->aSlot[iSlot];
#13354|   
#13355|     switch( pSlot->eType ){', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-40412

', 'Assignment of a potentially negative value to `iSlot` (line #13351) and its subsequent use as an index for `p->aSlot` (line #13353) poses a risk of out-of-bounds access, as assertions (line #13352) may be disabled or non-terminating, directly linking to the CVE''s described vulnerability.', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (373, 'sqlite-3.45.1-2.el10', 42, 'Error: OVERRUN (CWE-119):
sqlite-src-3450100/sqlite3.c:78529: cond_at_most: Checking "k > 5" implies that "k" may be up to 5 on the false branch.
sqlite-src-3450100/sqlite3.c:78524: cond_between: Checking "i < k" implies that "i" is between 2 and 4 (inclusive) on the true branch.
sqlite-src-3450100/sqlite3.c:78542: overrun-local: Overrunning array "szNew" of 5 4-byte elements at element index 5 (byte offset 23) using index "i + 1" (which evaluates to 5).
#78540|           }
#78541|         }
#78542|->       szNew[i+1] += sz;
#78543|         cntNew[i]--;
#78544|       }', true, 'Non-Issue', 'The is this check: 
 8388       if( i+1>=k ){
 8389         k = i+2;
 8390         if( k>NB+2 ){ rc = SQLITE_CORRUPT_BKPT; goto balance_cleanup; }
 8391         szNew[k-1] = 0;
 8392         cntNew[k-1] = b.nCell;
 8393       }
that send program to balance cleanup if k>5. And if k==5 on line 8390, then based od 8389 i=3.
That means accessing szNew[i+1] is valid.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (374, 'sqlite-3.45.1-2.el10', 43, 'Error: BAD_FREE (CWE-590):
sqlite-src-3450100/tool/sqldiff.c:1826: array_free: "sqlite3_result_text" frees array "zToken".
# 1824|     zSql = gobble_token(zSql, zToken, sizeof(zToken));
# 1825|     
# 1826|->   sqlite3_result_text(pCtx, zToken, -1, SQLITE_TRANSIENT);
# 1827|   }
# 1828|', true, 'Non-Issue', 'Freeing is ommited when SQLITE_TRANSIENT is set.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (451, 'texinfo-7.1-2.el10', 6, 'Error: OVERRUN (CWE-119):
texinfo-7.1/info/session.c:2348: alloc_strlen: Allocating insufficient memory for the terminating null of the string. [Note: The source code implementation of the function has been overridden by a builtin model.]
# 2346|             if (defentry)
# 2347|               {
# 2348|->               prompt = xmalloc (strlen (defentry->label)
# 2349|                                   + strlen (_("Follow xref (%s): ")));
# 2350|                 sprintf (prompt, _("Follow xref (%s): "), defentry->label);', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-43595

', NULL, '2025-11-18 16:18:56.203498');
INSERT INTO public.ground_truth VALUES (375, 'sqlite-3.45.1-2.el10', 44, 'Error: INTEGER_OVERFLOW (CWE-190):
sqlite-src-3450100/shell.c:10458: cast_overflow: Truncation due to cast operation on "(szFile < 65536LL) ? szFile : 65536LL" from 64 to 32 bits.
sqlite-src-3450100/shell.c:10458: overflow_assign: "nRead" is assigned from "(int)((szFile < 65536LL) ? szFile : 65536LL)".
sqlite-src-3450100/shell.c:10460: overflow_sink: "nRead", which might have overflowed, is passed to "zipfileReadData(pFile, aRead, nRead, iOff, &pTab->base.zErrMsg)".
#10458|       nRead = (int)(MIN(szFile, ZIPFILE_BUFFER_SIZE));
#10459|       iOff = szFile - nRead;
#10460|->     rc = zipfileReadData(pFile, aRead, nRead, iOff, &pTab->base.zErrMsg);
#10461|     }else{
#10462|       nRead = (int)(MIN(nBlob, ZIPFILE_BUFFER_SIZE));', true, 'Non-Issue', 'On RHEL supported architectures is int 4B and this is not a problem.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (376, 'sqlite-3.45.1-2.el10', 45, 'Error: BAD_FREE (CWE-763):
sqlite-src-3450100/sqlite3.c:143889: array_address: Taking address of array ""rowid"".
sqlite-src-3450100/sqlite3.c:143889: assign: Assigning: "zCol" = ""rowid"".
sqlite-src-3450100/sqlite3.c:143898: incorrect_free: "sqlite3VdbeSetColName" frees incorrect pointer "zCol".
#143896|           sqlite3VdbeSetColName(v, i, COLNAME_NAME, zName, SQLITE_DYNAMIC);
#143897|         }else{
#143898|->         sqlite3VdbeSetColName(v, i, COLNAME_NAME, zCol, SQLITE_TRANSIENT);
#143899|         }
#143900|       }else{', true, 'Non-Issue', 'Setting SQLITE_TRANSIENT ensures the memory is not freed.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (377, 'sqlite-3.45.1-2.el10', 46, 'Error: UNINIT (CWE-457):
sqlite-src-3450100/sqlite3_analyzer.c:204869: skipped_decl: Jumping over declaration of "opcode".
sqlite-src-3450100/sqlite3_analyzer.c:204923: uninit_use_in_call: Using uninitialized value "opcode" when calling "jsonBlobAppendNode".
#204921|         j++;
#204922|       }
#204923|->     jsonBlobAppendNode(pParse, opcode, j-1-i, &z[i+1]);
#204924|       return j+1;
#204925|     }', true, 'Non-Issue', 'opcode is initialized everytime the code steps into parse_string.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (378, 'sqlite-3.45.1-2.el10', 47, 'Error: USE_AFTER_FREE (CWE-416):
sqlite-src-3450100/sqlite3_analyzer.c:232787: freed_arg: "sqlite3_result_text" frees "ctx.zOut".
sqlite-src-3450100/sqlite3_analyzer.c:232789: double_free: Calling "sqlite3_free" frees pointer "ctx.zOut" which has already been freed. [Note: The source code implementation of the function has been overridden by a builtin model.]
#232787|         sqlite3_result_text(pCtx, (const char*)ctx.zOut, -1, SQLITE_TRANSIENT);
#232788|       }
#232789|->     sqlite3_free(ctx.zOut);
#232790|     }
#232791|     if( rc!=SQLITE_OK ){', true, 'Non-Issue', 'Setting SQLITE_TRANSIENT ensures the memory is not freed.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (379, 'sqlite-3.45.1-2.el10', 48, 'Error: RETURN_LOCAL (CWE-562):
sqlite-src-3450100/sqlite3.c:81127: local_ptr_identity_local: "sqlite3StrAccumInit(&sCheck.errMsg, NULL, zErr, 100, 1000000000)" stores "zErr" (address of local variable "zErr") into "sCheck.errMsg.zText".
sqlite-src-3450100/sqlite3.c:81227: identity_transfer: Passing field "sCheck.errMsg.zText" (indirectly, via argument 1) to function "sqlite3StrAccumFinish", which returns it.
sqlite-src-3450100/sqlite3.c:81227: escape_local_addr_alias: Returning, through "*pzOut", pointer "sqlite3StrAccumFinish(&sCheck.errMsg)" which points to local variable "zErr".
#81225|       *pzOut = 0;
#81226|     }else{
#81227|->     *pzOut = sqlite3StrAccumFinish(&sCheck.errMsg);
#81228|     }
#81229|     /* Make sure this analysis did not leave any unref() pages. */', true, 'Non-Issue', 'sqlite checks is the array is malloced, and if not reallocs it and returns the reallocated one.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (390, 'sqlite-3.45.1-2.el10', 59, 'Error: USE_AFTER_FREE (CWE-416):
sqlite-src-3450100/shell.c:10563: freed_arg: "zipfileResetCursor" frees "pCsr->pFreeEntry".
sqlite-src-3450100/shell.c:10606: use_after_free: Using freed pointer "pCsr->pFreeEntry".
#10604|     }else{
#10605|       pCsr->bNoop = 1;
#10606|->     pCsr->pCurrent = pCsr->pFreeEntry ? pCsr->pFreeEntry : pTab->pFirstEntry;
#10607|       rc = zipfileNext(cur);
#10608|     }', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-40412

I think pCsr->pFreeEntry should be set to NULL after freeing, and is not.', 'Direct use-after-free vulnerability at `sqlite-src-3450100/shell.c:10606`, where potentially freed `pCsr->pFreeEntry` (freed at `#10563`) is assigned to `pCsr->pCurrent` without explicit nullification or safeguarding after free.', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (380, 'sqlite-3.45.1-2.el10', 49, 'Error: INTEGER_OVERFLOW (CWE-190):
sqlite-src-3450100/sqlite3.c:201035: cast_overflow: Truncation due to cast operation on "pPhrase->iHead" from 64 to 32 bits.
sqlite-src-3450100/sqlite3.c:201035: overflow_assign: "iEnd" is assigned from "pPhrase->iHead".
sqlite-src-3450100/sqlite3.c:201042: overflow: The expression "iEnd - pIter->nSnippet" is deemed overflowed because at least one of its arguments has overflowed.
sqlite-src-3450100/sqlite3.c:201042: overflow: The expression "iEnd - pIter->nSnippet + 1" is deemed underflowed because at least one of its arguments has underflowed.
sqlite-src-3450100/sqlite3.c:201042: assign: Assigning: "iStart" = "iEnd - pIter->nSnippet + 1".
sqlite-src-3450100/sqlite3.c:201046: overflow_sink: "iStart", which might have underflowed, is passed to "fts3SnippetAdvance(&pPhrase->pTail, &pPhrase->iTail, iStart)".
#201044|         SnippetPhrase *pPhrase = &pIter->aPhrase[i];
#201045|         fts3SnippetAdvance(&pPhrase->pHead, &pPhrase->iHead, iEnd+1);
#201046|->       fts3SnippetAdvance(&pPhrase->pTail, &pPhrase->iTail, iStart);
#201047|       }
#201048|     }', true, 'Non-Issue', 'we are casting u32 to s32, but only when the u32 is less than 0x7FFFFFFF;
this will not cause overflow.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (381, 'sqlite-3.45.1-2.el10', 50, 'Error: USE_AFTER_FREE (CWE-416):
sqlite-src-3450100/sqlite3_analyzer.c:116611: assign: Assigning: "zBuf1" = "&zOut[nSql * 2LL + 1LL]".
sqlite-src-3450100/sqlite3_analyzer.c:116612: assign: Assigning: "zBuf2" = "&zOut[nSql * 4LL + 2LL]".
sqlite-src-3450100/sqlite3_analyzer.c:116651: assign: Assigning: "zReplace" = "zBuf2".
sqlite-src-3450100/sqlite3_analyzer.c:116667: freed_arg: "sqlite3_result_text" frees "zOut".
sqlite-src-3450100/sqlite3_analyzer.c:116668: pass_freed_arg: Passing freed pointer "zOut" as an argument to "sqlite3DbFree".
#116666|   
#116667|       sqlite3_result_text(pCtx, zOut, -1, SQLITE_TRANSIENT);
#116668|->     sqlite3DbFree(db, zOut);
#116669|     }else{
#116670|       rc = SQLITE_NOMEM;', true, 'Non-Issue', 'when the SQLITE_TRANSIENT flag is set, the  array is not freed.
That is also this case.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (382, 'sqlite-3.45.1-2.el10', 51, 'Error: USE_AFTER_FREE (CWE-416):
sqlite-src-3450100/sqlite3_analyzer.c:237132: freed_arg: "fts5ExprAddChildren" frees "pLeft".
sqlite-src-3450100/sqlite3_analyzer.c:237149: double_free: Calling "sqlite3Fts5ParseNodeFree" frees pointer "pLeft" which has already been freed.
#237147|     if( pRet==0 ){
#237148|       assert( pParse->rc!=SQLITE_OK );
#237149|->     sqlite3Fts5ParseNodeFree(pLeft);
#237150|       sqlite3Fts5ParseNodeFree(pRight);
#237151|       sqlite3Fts5ParseNearsetFree(pNear);', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-40412

Fix could be to set the pointer to NULL after freeing.', 'Double-free vulnerability (CWE-416) is possible as `fts5ExprAddChildren` frees `pLeft` (fts5_expr.c:2267) under certain conditions, and `sqlite3Fts5ParseNodeFree(pLeft)` is later called (sqlite3_analyzer.c:237149) without resetting the pointer to 0 or explicit NOOP evidence.', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (383, 'sqlite-3.45.1-2.el10', 52, 'Error: BAD_FREE (CWE-763):
sqlite-src-3450100/sqlite3_analyzer.c:143913: array_address: Taking address of array ""rowid"".
sqlite-src-3450100/sqlite3_analyzer.c:143913: assign: Assigning: "zCol" = ""rowid"".
sqlite-src-3450100/sqlite3_analyzer.c:143922: incorrect_free: "sqlite3VdbeSetColName" frees incorrect pointer "zCol".
#143920|           sqlite3VdbeSetColName(v, i, COLNAME_NAME, zName, SQLITE_DYNAMIC);
#143921|         }else{
#143922|->         sqlite3VdbeSetColName(v, i, COLNAME_NAME, zCol, SQLITE_TRANSIENT);
#143923|         }
#143924|       }else{', true, 'Non-Issue', 'when the SQLITE_TRANSIENT flag is set, the array is not freed. That is also this case.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (384, 'sqlite-3.45.1-2.el10', 53, 'Error: USE_AFTER_FREE (CWE-416):
sqlite-src-3450100/shell.c:13847: assign: Assigning: "pScanOrig" = "p->pScan".
sqlite-src-3450100/shell.c:13882: freed_arg: "idxScanFree" frees "p->pScan".
sqlite-src-3450100/shell.c:13884: use_after_free: Using freed pointer "pScanOrig".
#13882|       idxScanFree(p->pScan, pScanOrig);
#13883|       idxStatementFree(p->pStatement, pStmtOrig);
#13884|->     p->pScan = pScanOrig;
#13885|       p->pStatement = pStmtOrig;
#13886|     }', true, 'Non-Issue', 'pScanOrig is not freed, because it is the pLast, and thus p->pScan is nto freed, as they point to the same address.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (385, 'sqlite-3.45.1-2.el10', 54, 'Error: BAD_FREE (CWE-590):
sqlite-src-3450100/shell.c:7636: array_free: "sqlite3_result_text" frees array "z".
# 7634|     }
# 7635|     z[10] = ''\0'';
# 7636|->   sqlite3_result_text(context, z, -1, SQLITE_TRANSIENT);
# 7637|   }
# 7638|', true, 'Non-Issue', 'when the SQLITE_TRANSIENT flag is set, the  array is not freed.
That is also this case.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (386, 'sqlite-3.45.1-2.el10', 55, 'Error: USE_AFTER_FREE (CWE-416):
sqlite-src-3450100/sqlite3_analyzer.c:154750: freed_arg: "sqlite3DeleteTable" frees "pTab->zName".
sqlite-src-3450100/sqlite3_analyzer.c:154772: pass_freed_arg: Passing freed pointer "pTab->zName" as an argument to "sqlite3MPrintf".
#154770|       if( sCtx.bDeclared==0 ){
#154771|         const char *zFormat = "vtable constructor did not declare schema: %s";
#154772|->       *pzErr = sqlite3MPrintf(db, zFormat, pTab->zName);
#154773|         sqlite3VtabUnlock(pVTable);
#154774|         rc = SQLITE_ERROR;', false, 'Non-Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-40412

', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (387, 'sqlite-3.45.1-2.el10', 56, 'Error: OVERRUN (CWE-119):
sqlite-src-3450100/sqlite3.c:77725: cond_at_most: Checking "k < 6" implies that "k" may be up to 5 on the true branch.
sqlite-src-3450100/sqlite3.c:77756: incr: Incrementing "k". The value of "k" may now be up to 6.
sqlite-src-3450100/sqlite3.c:77757: overrun-local: Overrunning array "pCArray->apEnd" of 6 8-byte elements at element index 6 (byte offset 55) using index "k" (which evaluates to 6).
#77755|       if( pCArray->ixNx[k]<=i ){
#77756|         k++;
#77757|->       pEnd = pCArray->apEnd[k];
#77758|       }
#77759|     }', true, 'Non-Issue', 'As per upstream this is not a bug:
https://sqlite.org/forum/forumpost/d0e144c233c7b286 
Upstream also added some asserts to future releases so that static analysers are not confused.
In my opinion these asserts do not need to be backported.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (388, 'sqlite-3.45.1-2.el10', 57, 'Error: USE_AFTER_FREE (CWE-416):
sqlite-src-3450100/sqlite3_analyzer.c:87915: freed_arg: "sqlite3VdbeTransferError" frees "p->zErrMsg".
sqlite-src-3450100/sqlite3_analyzer.c:87932: pass_freed_arg: Passing freed pointer "p->zErrMsg" as an argument to "sqlite3DbFree".
#87930|   #endif
#87931|     if( p->zErrMsg ){
#87932|->     sqlite3DbFree(db, p->zErrMsg);
#87933|       p->zErrMsg = 0;
#87934|     }', true, 'Non-Issue', 'destructor function is set to SQLITE_TRANSIENT.
That means freeing does not take place in sqlite3VdbeTransferError in this case.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (391, 'sqlite-3.45.1-2.el10', 60, 'Error: USE_AFTER_FREE (CWE-416):
sqlite-src-3450100/sqlite3.c:237108: freed_arg: "fts5ExprAddChildren" frees "pLeft".
sqlite-src-3450100/sqlite3.c:237125: double_free: Calling "sqlite3Fts5ParseNodeFree" frees pointer "pLeft" which has already been freed.
#237123|     if( pRet==0 ){
#237124|       assert( pParse->rc!=SQLITE_OK );
#237125|->     sqlite3Fts5ParseNodeFree(pLeft);
#237126|       sqlite3Fts5ParseNodeFree(pRight);
#237127|       sqlite3Fts5ParseNearsetFree(pNear);', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-40412

Fix could be setting freed pointer to null.', 'Double-free vulnerability (CWE-416) confirmed: `fts5ExprAddChildren` frees `pLeft` at #237108, with no subsequent NULL assignment, and is later attempted to be freed again by `sqlite3Fts5ParseNodeFree` at #237125, within the execution path conditioned by `if( pRet==0 )` at #237123.', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (392, 'sqlite-3.45.1-2.el10', 61, 'Error: USE_AFTER_FREE (CWE-416):
sqlite-src-3450100/shell.c:13848: assign: Assigning: "pStmtOrig" = "p->pStatement".
sqlite-src-3450100/shell.c:13883: freed_arg: "idxStatementFree" frees "p->pStatement".
sqlite-src-3450100/shell.c:13885: use_after_free: Using freed pointer "pStmtOrig".
#13883|       idxStatementFree(p->pStatement, pStmtOrig);
#13884|       p->pScan = pScanOrig;
#13885|->     p->pStatement = pStmtOrig;
#13886|     }
#13887|', true, 'Non-Issue', 'pStmtOrig is not freed in idxStatementFree', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (393, 'sqlite-3.45.1-2.el10', 62, 'Error: OVERRUN (CWE-119):
sqlite-src-3450100/shell.c:27449: overrun-buffer-val: Overrunning array "p->nullValue" of 20 bytes by passing it to a function which accesses it at byte offset 2147483646.
#27447|       }
#27448|       oputf("%12.12s: ", "nullvalue");
#27449|->     output_c_string(p->nullValue);
#27450|       oputz("\n");
#27451|       oputf("%12.12s: %s\n","output",', true, 'Non-Issue', 'in anyOfInStr is check to not access it on ~(size_t)0 position.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (394, 'sqlite-3.45.1-2.el10', 63, 'Error: OVERRUN (CWE-119):
sqlite-src-3450100/sqlite3_analyzer.c:77749: cond_const: Checking "k < 6" implies that "k" is 6 on the false branch.
sqlite-src-3450100/sqlite3_analyzer.c:77750: overrun-local: Overrunning array "pCArray->apEnd" of 6 8-byte elements at element index 6 (byte offset 55) using index "k" (which evaluates to 6).
#77748|     if( iEnd<=iFirst ) return 0;
#77749|     for(k=0; ALWAYS(k<NB*2) && pCArray->ixNx[k]<=i ; k++){}
#77750|->   pEnd = pCArray->apEnd[k];
#77751|     while( 1 /*Exit by break*/ ){
#77752|       int sz, rc;', true, 'Non-Issue', 'As per upstream this is not a bug:
https://sqlite.org/forum/forumpost/d0e144c233c7b286 
Upstream also added some asserts to future releases so that static analysers are not confused.
In my opinion these asserts do not need to be backported.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (395, 'sqlite-3.45.1-2.el10', 64, 'Error: OVERRUN (CWE-119):
sqlite-src-3450100/sqlite3_analyzer.c:77666: cond_const: Checking "k < 6" implies that "k" is 6 on the false branch.
sqlite-src-3450100/sqlite3_analyzer.c:77667: overrun-local: Overrunning array "pCArray->apEnd" of 6 8-byte elements at element index 6 (byte offset 55) using index "k" (which evaluates to 6).
#77665|   
#77666|     for(k=0; ALWAYS(k<NB*2) && pCArray->ixNx[k]<=i; k++){}
#77667|->   pSrcEnd = pCArray->apEnd[k];
#77668|   
#77669|     pData = pEnd;', true, 'Non-Issue', 'As per upstream this is not a bug:
https://sqlite.org/forum/forumpost/d0e144c233c7b286 
Upstream also added some asserts to future releases so that static analysers are not confused.
In my opinion these asserts do not need to be backported.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (396, 'sqlite-3.45.1-2.el10', 65, 'Error: USE_AFTER_FREE (CWE-416):
sqlite-src-3450100/sqlite3.c:87891: freed_arg: "sqlite3VdbeTransferError" frees "p->zErrMsg".
sqlite-src-3450100/sqlite3.c:87908: double_free: Calling "sqlite3DbFree" frees pointer "p->zErrMsg" which has already been freed.
#87906|   #endif
#87907|     if( p->zErrMsg ){
#87908|->     sqlite3DbFree(db, p->zErrMsg);
#87909|       p->zErrMsg = 0;
#87910|     }', true, 'Issue', 'destructor function is set to SQLITE_TRANSIENT.
That means freeing does not take place in sqlite3VdbeTransferError in this case.', 'Code at line 87908 attempts to free potentially already freed `p->zErrMsg` pointer (as indicated by the CVE report snippet), with no explicit null or double-free prevention within the `sqlite3DbFree` function''s provided implementation, establishing a direct link to the described double-free vulnerability.', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (397, 'sqlite-3.45.1-2.el10', 66, 'Error: USE_AFTER_FREE (CWE-416):
sqlite-src-3450100/shell.c:7876: freed_arg: "sqlite3_result_text" frees "aBuf".
sqlite-src-3450100/shell.c:7877: double_free: Calling "sqlite3_free" frees pointer "aBuf" which has already been freed. [Note: The source code implementation of the function has been overridden by a builtin model.]
# 7875|   
# 7876|           sqlite3_result_text(ctx, aBuf, n, SQLITE_TRANSIENT);
# 7877|->         if( aBuf!=aStatic ) sqlite3_free(aBuf);
# 7878|   #endif
# 7879|         }else{', true, 'Non-Issue', 'Setting SQLITE_TRANSIENT ensures the memory is not freed.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (398, 'sqlite-3.45.1-2.el10', 67, 'Error: INTEGER_OVERFLOW (CWE-190):
sqlite-src-3450100/sqlite3.c:127622: cast_overflow: Truncation due to cast operation on "iCol" from 32 to 16 bits.
sqlite-src-3450100/sqlite3.c:127623: overflow_sink: "iCol", which might have overflowed, is passed to "sqlite3ExprCodeGetColumnOfTable(v, pTab, iDataCur, iCol, iOld + kk + 1)".
#127621|         if( mask==0xffffffff || (iCol<=31 && (mask & MASKBIT32(iCol))!=0) ){
#127622|           int kk = sqlite3TableColumnToStorage(pTab, iCol);
#127623|->         sqlite3ExprCodeGetColumnOfTable(v, pTab, iDataCur, iCol, iOld+kk+1);
#127624|         }
#127625|       }', true, 'Non-Issue', 'iCol in this case can not be bigger that i16, as the condition in for loop look looks as follows:
for(iCol=0; iCol<pTab->nCol; iCol++){
and pTab->nCol is i16', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (399, 'sqlite-3.45.1-2.el10', 68, 'Error: OVERRUN (CWE-119):
sqlite-src-3450100/sqlite3_analyzer.c:77749: cond_at_most: Checking "k < 6" implies that "k" may be up to 5 on the true branch.
sqlite-src-3450100/sqlite3_analyzer.c:77780: incr: Incrementing "k". The value of "k" may now be up to 6.
sqlite-src-3450100/sqlite3_analyzer.c:77779: overrun-local: Overrunning array "pCArray->ixNx" of 6 4-byte elements at element index 6 (byte offset 27) using index "k" (which evaluates to 6).
#77777|       i++;
#77778|       if( i>=iEnd ) break;
#77779|->     if( pCArray->ixNx[k]<=i ){
#77780|         k++;
#77781|         pEnd = pCArray->apEnd[k];', true, 'Non-Issue', 'As per upstream this is not a bug: https://sqlite.org/forum/forumpost/d0e144c233c7b286 Upstream also added some asserts to future releases so that static analysers are not confused. In my opinion these asserts do not need to be backported.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (400, 'sqlite-3.45.1-2.el10', 69, 'Error: USE_AFTER_FREE (CWE-416):
sqlite-src-3450100/sqlite3.c:154726: freed_arg: "sqlite3DeleteTable" frees "pTab->zName".
sqlite-src-3450100/sqlite3.c:154748: pass_freed_arg: Passing freed pointer "pTab->zName" as an argument to "sqlite3MPrintf".
#154746|       if( sCtx.bDeclared==0 ){
#154747|         const char *zFormat = "vtable constructor did not declare schema: %s";
#154748|->       *pzErr = sqlite3MPrintf(db, zFormat, pTab->zName);
#154749|         sqlite3VtabUnlock(pVTable);
#154750|         rc = SQLITE_ERROR;', true, 'Non-Issue', '54eb54c7de06e050023d97dc521e77308bf3df64
^this is a commit where upstream addresses this issue, even thougth they claim this is false positive.
more info in a forum thread: https://sqlite.org/forum/forumpost/cafbe582e8', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (401, 'sqlite-3.45.1-2.el10', 70, 'Error: OVERRUN (CWE-119):
sqlite-src-3450100/sqlite3_analyzer.c:186463: buffer_alloc: Calling allocating function "sqlite3_realloc64" which allocates "nAlloc" bytes. [Note: The source code implementation of the function has been overridden by a builtin model.]
sqlite-src-3450100/sqlite3_analyzer.c:186463: var_assign: Assigning: "zNew" = "sqlite3_realloc64(zBuffer, nAlloc)".
sqlite-src-3450100/sqlite3_analyzer.c:186468: alias: Assigning: "zBuffer" = "zNew".
sqlite-src-3450100/sqlite3_analyzer.c:186460: symbolic_compare: Tracking "(i64)nPrefix + nSuffix" since "nAlloc" is tracked with "zNew".
sqlite-src-3450100/sqlite3_analyzer.c:186460: symbolic_compare: Tracking "(i64)nPrefix + nSuffix" since "nAlloc" is tracked with "zBuffer".
sqlite-src-3450100/sqlite3_analyzer.c:186472: symbolic_assign: Tracking "nBuffer" since "nPrefix + nSuffix" is tracked with "zNew".
sqlite-src-3450100/sqlite3_analyzer.c:186472: symbolic_assign: Tracking "nBuffer" since "nPrefix + nSuffix" is tracked with "zBuffer".
sqlite-src-3450100/sqlite3_analyzer.c:186447: symbolic_compare: Tracking "nPrefix" since "nBuffer" is tracked with "zNew".
sqlite-src-3450100/sqlite3_analyzer.c:186447: symbolic_compare: Tracking "nPrefix" since "nBuffer" is tracked with "zBuffer".
sqlite-src-3450100/sqlite3_analyzer.c:186456: symbolic_compare: Tracking "zCsr - zNode" since "nPrefix" is tracked with "zNew".
sqlite-src-3450100/sqlite3_analyzer.c:186456: symbolic_compare: Tracking "nPrefix" since "zCsr - zNode" is tracked with "zBuffer".
sqlite-src-3450100/sqlite3_analyzer.c:186460: symbolic_compare: Tracking "(i64)nPrefix + nSuffix" since "nAlloc" is tracked with "zNew".
sqlite-src-3450100/sqlite3_analyzer.c:186460: symbolic_compare: Tracking "(i64)nPrefix + nSuffix" since "nAlloc" is tracked with "zBuffer".
sqlite-src-3450100/sqlite3_analyzer.c:186471: overrun-local: Overrunning dynamic array "zBuffer" at offset corresponding to index variable "nPrefix".
#186469|       }
#186470|       assert( zBuffer );
#186471|->     memcpy(&zBuffer[nPrefix], zCsr, nSuffix);
#186472|       nBuffer = nPrefix + nSuffix;
#186473|       zCsr += nSuffix;', true, 'Non-Issue', 'length of zBuffer is always bigger that nPrefix.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (452, 'texinfo-7.1-2.el10', 7, 'Error: RESOURCE_LEAK (CWE-772):
texinfo-7.1/install-info/install-info.c:880: alloc_fn: Storage is returned from allocation function "xmalloc". [Note: The source code implementation of the function has been overridden by a builtin model.]
texinfo-7.1/install-info/install-info.c:880: var_assign: Assigning: "data" = storage returned from "xmalloc(data_size + 1)".
texinfo-7.1/install-info/install-info.c:888: leaked_storage: Variable "data" going out of scope leaks the storage it points to.
#  886|   
#  887|     if (!f)
#  888|->     return 0;
#  889|   
#  890|     for (;;)', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-43595

', NULL, '2025-11-18 16:18:56.203498');
INSERT INTO public.ground_truth VALUES (402, 'sqlite-3.45.1-2.el10', 71, 'Error: OVERRUN (CWE-119):
sqlite-src-3450100/sqlite3_analyzer.c:77749: cond_at_most: Checking "k < 6" implies that "k" may be up to 5 on the true branch.
sqlite-src-3450100/sqlite3_analyzer.c:77780: incr: Incrementing "k". The value of "k" may now be up to 6.
sqlite-src-3450100/sqlite3_analyzer.c:77781: overrun-local: Overrunning array "pCArray->apEnd" of 6 8-byte elements at element index 6 (byte offset 55) using index "k" (which evaluates to 6).
#77779|       if( pCArray->ixNx[k]<=i ){
#77780|         k++;
#77781|->       pEnd = pCArray->apEnd[k];
#77782|       }
#77783|     }', true, 'Non-Issue', 'As per upstream this is not a bug:
https://sqlite.org/forum/forumpost/d0e144c233c7b286 
Upstream also added some asserts to future releases so that static analysers are not confused.
In my opinion these asserts do not need to be backported.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (403, 'sqlite-3.45.1-2.el10', 72, 'Error: INTEGER_OVERFLOW (CWE-190):
sqlite-src-3450100/sqlite3_analyzer.c:31129: tainted_data_return: Called function "getIntArg(pArgList)", and a possible return value is known to be less than zero.
sqlite-src-3450100/sqlite3_analyzer.c:31129: cast_overflow: Truncation due to cast operation on "getIntArg(pArgList)" from 63 to 32 bits.
sqlite-src-3450100/sqlite3_analyzer.c:31129: overflow_assign: "width" is assigned from "(int)getIntArg(pArgList)".
sqlite-src-3450100/sqlite3_analyzer.c:31723: overflow: The expression "width -= length" is deemed overflowed because at least one of its arguments has overflowed.
sqlite-src-3450100/sqlite3_analyzer.c:31725: overflow_sink: "width", which might have underflowed, is passed to "sqlite3_str_appendchar(pAccum, width, '' '')".
#31723|       width -= length;
#31724|       if( width>0 ){
#31725|->       if( !flag_leftjustify ) sqlite3_str_appendchar(pAccum, width, '' '');
#31726|         sqlite3_str_append(pAccum, bufpt, length);
#31727|         if( flag_leftjustify ) sqlite3_str_appendchar(pAccum, width, '' '');', true, 'Non-Issue', 'there is check if width is bigger than 0', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (404, 'sqlite-3.45.1-2.el10', 73, 'Error: USE_AFTER_FREE (CWE-416):
sqlite-src-3450100/sqlite3_analyzer.c:116611: assign: Assigning: "zBuf1" = "&zOut[nSql * 2LL + 1LL]".
sqlite-src-3450100/sqlite3_analyzer.c:116612: assign: Assigning: "zBuf2" = "&zOut[nSql * 4LL + 2LL]".
sqlite-src-3450100/sqlite3_analyzer.c:116667: freed_arg: "sqlite3_result_text" frees "zOut".
sqlite-src-3450100/sqlite3_analyzer.c:116668: double_free: Calling "sqlite3DbFree" frees pointer "zOut" which has already been freed.
#116666|   
#116667|       sqlite3_result_text(pCtx, zOut, -1, SQLITE_TRANSIENT);
#116668|->     sqlite3DbFree(db, zOut);
#116669|     }else{
#116670|       rc = SQLITE_NOMEM;', true, 'Non-Issue', 'Setting SQLITE_TRANSIENT ensures the memory is not freed.', 'Assignment of `zOut` to `sqlite3_result_text` with `SQLITE_TRANSIENT` flag (line #116667) ensures its memory is not freed, thus preventing the reported double-free vulnerability at line #116668 (`sqlite3DbFree(db, zOut)`).', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (405, 'sqlite-3.45.1-2.el10', 74, 'Error: INTEGER_OVERFLOW (CWE-190):
sqlite-src-3450100/shell.c:10114: tainted_data_argument: The value "*aRead" is considered tainted.
sqlite-src-3450100/shell.c:10137: tainted_data_transitive: Call to function "zipfileReadCDS" with tainted argument "*aRead" transitively taints "pNew->cds.iOffset".
sqlite-src-3450100/shell.c:10179: underflow: The cast of "pNew->cds.iOffset" to a signed type could result in a negative number.
#10177|           }
#10178|         }else{
#10179|->         *pzErr = sqlite3_mprintf("failed to read LFH at offset %d", 
#10180|               (int)pNew->cds.iOffset
#10181|           );', true, 'Non-Issue', 'assigning u16 to i32 does not cause overflow.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (406, 'sqlite-3.45.1-2.el10', 75, 'Error: USE_AFTER_FREE (CWE-416):
sqlite-src-3450100/sqlite3_analyzer.c:66030: freed_arg: "walIndexAppend" frees "pWal->apWiData".
sqlite-src-3450100/sqlite3_analyzer.c:66044: use_after_free: Using freed pointer "pWal->apWiData".
#66042|           }
#66043|         }
#66044|->       pWal->apWiData[iPg] = aShare;
#66045|         SEH_SET_ON_ERROR(0,0);
#66046|         nHdr = (iPg==0 ? WALINDEX_HDR_SIZE : 0);', true, 'Non-Issue', 'It is realloc, not free.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (407, 'sqlite-3.45.1-2.el10', 76, 'Error: OVERRUN (CWE-119):
sqlite-src-3450100/shell.c:27457: overrun-buffer-val: Overrunning array "p->rowSeparator" of 20 bytes by passing it to a function which accesses it at byte offset 2147483646.
#27455|        oputz("\n");
#27456|       oputf("%12.12s: ", "rowseparator");
#27457|->      output_c_string(p->rowSeparator);
#27458|        oputz("\n");
#27459|       switch( p->statsOn ){', true, 'Non-Issue', 'in anyOfInStr is check to not access it on ~(size_t)0 position.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (408, 'sqlite-3.45.1-2.el10', 77, 'Error: BAD_FREE (CWE-590):
sqlite-src-3450100/shell.c:5436: array_free: "sqlite3_result_text" frees array "zResult".
# 5434|           sqlite3_snprintf(sizeof(zResult), zResult, "ieee754(%lld,%d)",
# 5435|                            m, e-1075);
# 5436|->         sqlite3_result_text(context, zResult, -1, SQLITE_TRANSIENT);
# 5437|           break;
# 5438|         case 1:', true, 'Non-Issue', 'when the SQLITE_TRANSIENT flag is set, the  array is not freed.
That is also this case.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (409, 'sqlite-3.45.1-2.el10', 78, 'Error: INTEGER_OVERFLOW (CWE-190):
sqlite-src-3450100/sqlite3.c:201035: cast_overflow: Truncation due to cast operation on "pPhrase->iHead" from 64 to 32 bits.
sqlite-src-3450100/sqlite3.c:201035: overflow_assign: "iEnd" is assigned from "pPhrase->iHead".
sqlite-src-3450100/sqlite3.c:201045: overflow: The expression "iEnd + 1" is deemed overflowed because at least one of its arguments has overflowed.
sqlite-src-3450100/sqlite3.c:201045: overflow_sink: "iEnd + 1", which might have underflowed, is passed to "fts3SnippetAdvance(&pPhrase->pHead, &pPhrase->iHead, iEnd + 1)".
#201043|       for(i=0; i<pIter->nPhrase; i++){
#201044|         SnippetPhrase *pPhrase = &pIter->aPhrase[i];
#201045|->       fts3SnippetAdvance(&pPhrase->pHead, &pPhrase->iHead, iEnd+1);
#201046|         fts3SnippetAdvance(&pPhrase->pTail, &pPhrase->iTail, iStart);
#201047|       }', true, 'Non-Issue', 'we are casting u32 to s32, but only when the u32 is less than 0x7FFFFFFF;
this will not cause overflow.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (417, 'sqlite-3.45.1-2.el10', 86, 'Error: USE_AFTER_FREE (CWE-416):
sqlite-src-3450100/sqlite3_analyzer.c:237133: freed_arg: "fts5ExprAddChildren" frees "pRight".
sqlite-src-3450100/sqlite3_analyzer.c:237150: double_free: Calling "sqlite3Fts5ParseNodeFree" frees pointer "pRight" which has already been freed.
#237148|       assert( pParse->rc!=SQLITE_OK );
#237149|       sqlite3Fts5ParseNodeFree(pLeft);
#237150|->     sqlite3Fts5ParseNodeFree(pRight);
#237151|       sqlite3Fts5ParseNearsetFree(pNear);
#237152|     }', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-40412

Fix could be setting freed pointer to null.', 'Consecutive frees of `pRight` at lines 237133 and 237150, with no intervening nullification or reassignment, confirm a double-free vulnerability, lacking explicit safeguards or evidence of a harmless second free.', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (410, 'sqlite-3.45.1-2.el10', 79, 'Error: OVERRUN (CWE-119):
sqlite-src-3450100/sqlite3.c:210502: cond_const: Checking "iIdx < 40" implies that "iIdx" is 41 on the false branch.
sqlite-src-3450100/sqlite3.c:210561: overrun-buffer-arg: Overrunning array "zIdxStr" of 41 bytes by passing it to a function which accesses it at byte offset 41 using argument "iIdx + 1" (which evaluates to 42). [Note: The source code implementation of the function has been overridden by a builtin model.]
#210559|         return SQLITE_NOMEM;
#210560|       }
#210561|->     memcpy(pIdxInfo->idxStr, zIdxStr, iIdx+1);
#210562|     }
#210563|', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-40412

', 'Buffer overrun is implied at line 210561 when `iIdx` is 41 (as inferred from `iIdx < 40` check at line 210502), causing `memcpy` to access `zIdxStr` (size 41 bytes) at offset 42, with no explicit bounds checking to prevent this overrun.', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (411, 'sqlite-3.45.1-2.el10', 80, 'Error: RETURN_LOCAL (CWE-562):
sqlite-src-3450100/sqlite3_analyzer.c:81151: local_ptr_identity_local: "sqlite3StrAccumInit(&sCheck.errMsg, NULL, zErr, 100, 1000000000)" stores "zErr" (address of local variable "zErr") into "sCheck.errMsg.zText".
sqlite-src-3450100/sqlite3_analyzer.c:81251: identity_transfer: Passing field "sCheck.errMsg.zText" (indirectly, via argument 1) to function "sqlite3StrAccumFinish", which returns it.
sqlite-src-3450100/sqlite3_analyzer.c:81251: escape_local_addr_alias: Returning, through "*pzOut", pointer "sqlite3StrAccumFinish(&sCheck.errMsg)" which points to local variable "zErr".
#81249|       *pzOut = 0;
#81250|     }else{
#81251|->     *pzOut = sqlite3StrAccumFinish(&sCheck.errMsg);
#81252|     }
#81253|     /* Make sure this analysis did not leave any unref() pages. */', true, 'Non-Issue', 'It is returning integer', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (412, 'sqlite-3.45.1-2.el10', 81, 'Error: OVERLAPPING_COPY:
sqlite-src-3450100/sqlite3_analyzer.c:96007: assign: Assigning: "pLast" = "&pData0[nField - 1]".
sqlite-src-3450100/sqlite3_analyzer.c:96072: assign: Assigning: "pRec" = "pLast".
sqlite-src-3450100/sqlite3_analyzer.c:96133: equal: The address of "pRec->u.r" is equal to "pData0 + (nField - 1)".
sqlite-src-3450100/sqlite3_analyzer.c:96133: equal: The address of "pRec->u.i" is equal to "pData0 + (nField - 1)".
sqlite-src-3450100/sqlite3_analyzer.c:96133: overlapping_assignment: Assigning "pRec->u.i" to "pRec->u.r", which have overlapping memory locations and different types.
sqlite-src-3450100/sqlite3_analyzer.c:96133: target_type: "pRec->u.r" has type "double".
sqlite-src-3450100/sqlite3_analyzer.c:96133: source_type: "pRec->u.i" has type "long long".
#96131|             ** as an integer, then we might as well make it an 8-byte floating
#96132|             ** point value */
#96133|->           pRec->u.r = (double)pRec->u.i;
#96134|             pRec->flags &= ~MEM_IntReal;
#96135|             pRec->flags |= MEM_Real;', true, 'Issue', 'pIn1->u.i and pIn1->u.r are both members of the same union in Mem struct (Mem is typedef for sqlite3_value).
225 struct sqlite3_value {
226   union MemValue {
227     double r;           /* Real value used when MEM_Real is set in flags */
228     i64 i;              /* Integer value used when MEM_Int is set in flags */
229     int nZero;          /* Extra zero bytes when MEM_Zero and MEM_Blob set */
230     const char *zPType; /* Pointer type when MEM_Term|MEM_Subtype|MEM_Null */
231     FuncDef *pDef;      /* Used only when flags==MEM_Agg */
232   } u; 
This assingment is in COLTYPE_REAL case, setting flag MEM_Real, which means we need to have pIn1->u.r filled. ', 'Overlapping assignment with type mismatch occurs at line 96133 (`pRec->u.r = (double)pRec->u.i;`), where `double` and `long long` types share memory, with no mitigating checks in the provided code snippet.', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (413, 'sqlite-3.45.1-2.el10', 82, 'Error: USE_AFTER_FREE (CWE-416):
sqlite-src-3450100/sqlite3_analyzer.c:191115: freed_arg: "sqlite3_free" frees "pCsr->filter.zTerm". [Note: The source code implementation of the function has been overridden by a builtin model.]
sqlite-src-3450100/sqlite3_analyzer.c:191151: pass_freed_arg: Passing freed pointer "pCsr->filter.zTerm" as an argument to "sqlite3Fts3SegReaderCursor".
#191149|     pCsr->iLangid = iLangVal;
#191150|   
#191151|->   rc = sqlite3Fts3SegReaderCursor(pFts3, iLangVal, 0, FTS3_SEGCURSOR_ALL,
#191152|         pCsr->filter.zTerm, pCsr->filter.nTerm, 0, isScan, &pCsr->csr
#191153|     );', false, 'Non-Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-40412

', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (414, 'sqlite-3.45.1-2.el10', 83, 'Error: INTEGER_OVERFLOW (CWE-190):
sqlite-src-3450100/sqlite3.c:31105: tainted_data_return: Called function "getIntArg(pArgList)", and a possible return value is known to be less than zero.
sqlite-src-3450100/sqlite3.c:31105: cast_overflow: Truncation due to cast operation on "getIntArg(pArgList)" from 63 to 32 bits.
sqlite-src-3450100/sqlite3.c:31105: overflow_assign: "width" is assigned from "(int)getIntArg(pArgList)".
sqlite-src-3450100/sqlite3.c:31699: overflow: The expression "width -= length" is deemed overflowed because at least one of its arguments has overflowed.
sqlite-src-3450100/sqlite3.c:31701: overflow_sink: "width", which might have underflowed, is passed to "sqlite3_str_appendchar(pAccum, width, '' '')".
#31699|       width -= length;
#31700|       if( width>0 ){
#31701|->       if( !flag_leftjustify ) sqlite3_str_appendchar(pAccum, width, '' '');
#31702|         sqlite3_str_append(pAccum, bufpt, length);
#31703|         if( flag_leftjustify ) sqlite3_str_appendchar(pAccum, width, '' '');', true, 'Non-Issue', 'there is check if width is bigger than 0', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (415, 'sqlite-3.45.1-2.el10', 84, 'Error: OVERRUN (CWE-119):
sqlite-src-3450100/shell.c:27454: overrun-buffer-val: Overrunning array "p->colSeparator" of 20 bytes by passing it to a function which accesses it at byte offset 2147483646.
#27452|             strlen30(p->outfile) ? p->outfile : "stdout");
#27453|       oputf("%12.12s: ", "colseparator");
#27454|->      output_c_string(p->colSeparator);
#27455|        oputz("\n");
#27456|       oputf("%12.12s: ", "rowseparator");', true, 'Non-Issue', 'in anyOfInStr is check to not access it on ~(size_t)0 position.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (416, 'sqlite-3.45.1-2.el10', 85, 'Error: OVERRUN (CWE-119):
sqlite-src-3450100/sqlite3.c:77725: cond_at_most: Checking "k < 6" implies that "k" may be up to 5 on the true branch.
sqlite-src-3450100/sqlite3.c:77756: incr: Incrementing "k". The value of "k" may now be up to 6.
sqlite-src-3450100/sqlite3.c:77755: overrun-local: Overrunning array "pCArray->ixNx" of 6 4-byte elements at element index 6 (byte offset 27) using index "k" (which evaluates to 6).
#77753|       i++;
#77754|       if( i>=iEnd ) break;
#77755|->     if( pCArray->ixNx[k]<=i ){
#77756|         k++;
#77757|         pEnd = pCArray->apEnd[k];', true, 'Non-Issue', 'As per upstream this is not a bug:
https://sqlite.org/forum/forumpost/d0e144c233c7b286 
Upstream also added some asserts to future releases so that static analysers are not confused.
In my opinion these asserts do not need to be backported.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (418, 'sqlite-3.45.1-2.el10', 87, 'Error: INTEGER_OVERFLOW (CWE-190):
sqlite-src-3450100/shell.c:14849: cast_overflow: Truncation due to cast operation on "nPayload" from 64 to 32 bits.
sqlite-src-3450100/shell.c:14849: overflow_assign: "nLocal" is assigned from "nPayload".
sqlite-src-3450100/shell.c:14874: overflow_sink: "nLocal", which might have overflowed, is passed to "memcpy(pCsr->pRec, &pCsr->aPage[iOff], nLocal)". [Note: The source code implementation of the function has been overridden by a builtin model.]
#14872|   
#14873|               /* Load the nLocal bytes of payload */
#14874|->             memcpy(pCsr->pRec, &pCsr->aPage[iOff], nLocal);
#14875|               iOff += nLocal;
#14876|', true, 'Non-Issue', 'It cannot overflow, as there is check nPayload <=X, and X is int.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (419, 'sqlite-3.45.1-2.el10', 88, 'Error: OVERRUN (CWE-119):
sqlite-src-3450100/sqlite3.c:77642: cond_at_most: Checking "k < 6" implies that "k" may be up to 5 on the true branch.
sqlite-src-3450100/sqlite3.c:77668: incr: Incrementing "k". The value of "k" may now be up to 6.
sqlite-src-3450100/sqlite3.c:77667: overrun-local: Overrunning array "pCArray->ixNx" of 6 4-byte elements at element index 6 (byte offset 27) using index "k" (which evaluates to 6).
#77665|       i++;
#77666|       if( i>=iEnd ) break;
#77667|->     if( pCArray->ixNx[k]<=i ){
#77668|         k++;
#77669|         pSrcEnd = pCArray->apEnd[k];', true, 'Non-Issue', 'As per upstream this is not a bug:
https://sqlite.org/forum/forumpost/d0e144c233c7b286 
Upstream also added some asserts to future releases so that static analysers are not confused.
In my opinion these asserts do not need to be backported.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (420, 'sqlite-3.45.1-2.el10', 89, 'Error: COPY_PASTE_ERROR (CWE-398):
sqlite-src-3450100/sqlite3.c:170128: original: "pMWin->regStartRowid" looks like the original copy.
sqlite-src-3450100/sqlite3.c:170140: copy_paste_error: "regStartRowid" in "pMWin->regStartRowid" looks like a copy-paste error.
sqlite-src-3450100/sqlite3.c:170140: remediation: Should it say "regEndRowid" instead?
#170138|         csr = p->end.csr;
#170139|         reg = p->end.reg;
#170140|->       if( pMWin->regStartRowid ){
#170141|           assert( pMWin->regEndRowid );
#170142|           sqlite3VdbeAddOp2(v, OP_AddImm, pMWin->regEndRowid, 1);', true, 'Non-Issue', 'there is also assert for checking in regEndRowid is not NULL. That is sufficient.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (421, 'sqlite-3.45.1-2.el10', 90, 'Error: INTEGER_OVERFLOW (CWE-190):
sqlite-src-3450100/shell.c:10114: tainted_data_argument: The value "*aRead" is considered tainted.
sqlite-src-3450100/shell.c:10124: tainted_data_transitive: Call to function "zipfileGetU16" with tainted argument "*aRead" returns tainted data.
sqlite-src-3450100/shell.c:10124: cast_overflow: An assign that casts to a different type, which might trigger an overflow.
sqlite-src-3450100/shell.c:10125: overflow: The tainted expression "nExtra" is used in an arithmetic operation. The expression "nExtra" is considered to have possibly overflowed.
sqlite-src-3450100/shell.c:10127: overflow: The expression "96UL + nExtra" is deemed overflowed because at least one of its arguments has overflowed.
sqlite-src-3450100/shell.c:10127: cast_overflow: An assign that casts to a different type, which might trigger an overflow.
sqlite-src-3450100/shell.c:10132: overflow_sink: "nAlloc", which might have underflowed, is passed to "sqlite3_malloc64(nAlloc)". [Note: The source code implementation of the function has been overridden by a builtin model.]
#10130|       }
#10131|   
#10132|->     pNew = (ZipfileEntry*)sqlite3_malloc64(nAlloc);
#10133|       if( pNew==0 ){
#10134|         rc = SQLITE_NOMEM;', true, 'Non-Issue', 'assigning u16 to i32 does not cause overflow.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (422, 'sqlite-3.45.1-2.el10', 91, 'Error: OVERLAPPING_COPY:
sqlite-src-3450100/sqlite3.c:95802: assign: Assigning: "pIn1" = "&aMem[pOp->p1]".
sqlite-src-3450100/sqlite3.c:95841: equal: The address of "pIn1->u.r" is equal to "aMem + pOp->p1".
sqlite-src-3450100/sqlite3.c:95841: equal: The address of "pIn1->u.i" is equal to "aMem + pOp->p1".
sqlite-src-3450100/sqlite3.c:95841: overlapping_assignment: Assigning "pIn1->u.i" to "pIn1->u.r", which have overlapping memory locations and different types.
sqlite-src-3450100/sqlite3.c:95841: target_type: "pIn1->u.r" has type "double".
sqlite-src-3450100/sqlite3.c:95841: source_type: "pIn1->u.i" has type "long long".
#95839|                 pIn1->flags &= ~MEM_Int;
#95840|               }else{
#95841|->               pIn1->u.r = (double)pIn1->u.i;
#95842|                 pIn1->flags |= MEM_Real;
#95843|                 pIn1->flags &= ~MEM_Int;', true, 'Issue', 'members of the same union, of the same size.', 'Assignment at line #95841 potentially leads to data corruption or unexpected behavior due to overlapping memory locations of different types (`double` and `long long`) without ensuring sufficient memory allocation, directly correlating with the CVE''s described `overlapping_assignment` vulnerability.', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (466, 'texinfo-7.1-2.el10', 21, 'Error: UNINIT (CWE-457):
texinfo-7.1/info/infokey.c:211: var_decl: Declaring variable "seq" without initializer.
texinfo-7.1/info/infokey.c:450: uninit_use: Using uninitialized value "seq[0]".
#  448|                            this default binding is disabled with "#stop".)
#  449|                            E.g. do not allow "b menu-digit".  */
#  450|->                       if (seq[0] != ''1'' || seq[1] != ''\0''
#  451|                             || section != info)
#  452|                           {', false, 'Non-Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-43595

', NULL, '2025-11-18 16:18:56.203498');
INSERT INTO public.ground_truth VALUES (423, 'sqlite-3.45.1-2.el10', 92, 'Error: OVERRUN (CWE-119):
sqlite-src-3450100/shell.c:12217: strlen_assign: Setting variable "n" to the return value of strlen called with argument "zIn".
sqlite-src-3450100/shell.c:12218: alloc_strlen: Allocating insufficient memory for the terminating null of the string. [Note: The source code implementation of the function has been overridden by a builtin model.]
#12216|   static char *expertDequote(const char *zIn){
#12217|     int n = STRLEN(zIn);
#12218|->   char *zRet = sqlite3_malloc(n);
#12219|   
#12220|     assert( zIn[0]==''\'''' );', true, 'Non-Issue', 'the result string in smaller by 2 chars, because quotes are removed. The memory is sufficient.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (424, 'sqlite-3.45.1-2.el10', 93, 'Error: UNINIT (CWE-457):
sqlite-src-3450100/sqlite3.c:204845: skipped_decl: Jumping over declaration of "opcode".
sqlite-src-3450100/sqlite3.c:204875: uninit_use: Using uninitialized value "opcode".
#204873|              || c==''n'' || c==''r'' || c==''t''
#204874|              || (c==''u'' && jsonIs4Hex(&z[j+1])) ){
#204875|->           if( opcode==JSONB_TEXT ) opcode = JSONB_TEXTJ;
#204876|           }else if( c==''\'''' || c==''0'' || c==''v'' || c==''\n''
#204877|              || (0xe2==(u8)c && 0x80==(u8)z[j+1]', true, 'Non-Issue', 'opcode is intilialized before jumping to the parse_string section.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (425, 'sqlite-3.45.1-2.el10', 94, 'Error: USE_AFTER_FREE (CWE-416):
sqlite-src-3450100/sqlite3.c:82554: alias: Equality between "pMem->z" and "pMem->zMalloc" implies that they are aliases.
sqlite-src-3450100/sqlite3.c:82558: freed_arg: "sqlite3Realloc" frees "pMem->z".
sqlite-src-3450100/sqlite3.c:82559: double_free: Calling "sqlite3_free" frees pointer "pMem->z" which has already been freed. [Note: The source code implementation of the function has been overridden by a builtin model.]
#82557|       }else{
#82558|         pMem->zMalloc = sqlite3Realloc(pMem->z, n);
#82559|->       if( pMem->zMalloc==0 ) sqlite3_free(pMem->z);
#82560|         pMem->z = pMem->zMalloc;
#82561|       }', false, 'Non-Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-40412

', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (426, 'sqlite-3.45.1-2.el10', 95, 'Error: BAD_FREE (CWE-590):
sqlite-src-3450100/sqlite3.c:150465: address_free: "sqlite3_free_table" frees address of "res.azResult[1]".
sqlite-src-3450100/sqlite3.c:150478: address_free: "sqlite3_free_table" frees address of "res.azResult[1]".
sqlite-src-3450100/sqlite3.c:150485: address_free: "sqlite3_free_table" frees address of "res.azResult[1]".
#150463|     res.azResult[0] = SQLITE_INT_TO_PTR(res.nData);
#150464|     if( (rc&0xff)==SQLITE_ABORT ){
#150465|->     sqlite3_free_table(&res.azResult[1]);
#150466|       if( res.zErrMsg ){
#150467|         if( pzErrMsg ){', true, 'Non-Issue', 'res.azResult is preoperly allocated by malloc in line n.140 (src/table.c)', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (427, 'sqlite-3.45.1-2.el10', 96, 'Error: OVERRUN (CWE-119):
sqlite-src-3450100/sqlite3.c:77642: cond_at_most: Checking "k < 6" implies that "k" may be up to 5 on the true branch.
sqlite-src-3450100/sqlite3.c:77668: incr: Incrementing "k". The value of "k" may now be up to 6.
sqlite-src-3450100/sqlite3.c:77669: overrun-local: Overrunning array "pCArray->apEnd" of 6 8-byte elements at element index 6 (byte offset 55) using index "k" (which evaluates to 6).
#77667|       if( pCArray->ixNx[k]<=i ){
#77668|         k++;
#77669|->       pSrcEnd = pCArray->apEnd[k];
#77670|       }
#77671|     }', true, 'Non-Issue', 'As per upstream this is not a bug:
https://sqlite.org/forum/forumpost/d0e144c233c7b286 
Upstream also added some asserts to future releases so that static analysers are not confused.
In my opinion these asserts do not need to be backported.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (428, 'sqlite-3.45.1-2.el10', 97, 'Error: OVERLAPPING_COPY:
sqlite-src-3450100/sqlite3.c:95983: assign: Assigning: "pLast" = "&pData0[nField - 1]".
sqlite-src-3450100/sqlite3.c:96048: assign: Assigning: "pRec" = "pLast".
sqlite-src-3450100/sqlite3.c:96109: equal: The address of "pRec->u.r" is equal to "pData0 + (nField - 1)".
sqlite-src-3450100/sqlite3.c:96109: equal: The address of "pRec->u.i" is equal to "pData0 + (nField - 1)".
sqlite-src-3450100/sqlite3.c:96109: overlapping_assignment: Assigning "pRec->u.i" to "pRec->u.r", which have overlapping memory locations and different types.
sqlite-src-3450100/sqlite3.c:96109: target_type: "pRec->u.r" has type "double".
sqlite-src-3450100/sqlite3.c:96109: source_type: "pRec->u.i" has type "long long".
#96107|             ** as an integer, then we might as well make it an 8-byte floating
#96108|             ** point value */
#96109|->           pRec->u.r = (double)pRec->u.i;
#96110|             pRec->flags &= ~MEM_IntReal;
#96111|             pRec->flags |= MEM_Real;', true, 'Issue', 'Both u.r and u.i are members of the same union.', 'Overlapping assignment with type mismatch occurs at `sqlite-src-3450100/sqlite3.c:96109`, where `long long` `pRec->u.i` is cast to `double` `pRec->u.r` in overlapping memory locations (`pData0 + (nField - 1)`), with no explicit mitigation in the provided code.', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (429, 'sqlite-3.45.1-2.el10', 98, 'Error: USE_AFTER_FREE (CWE-416):
sqlite-src-3450100/sqlite3_analyzer.c:67037: freed_arg: "sqlite3WalCheckpoint" frees "pWal->apWiData".
sqlite-src-3450100/sqlite3_analyzer.c:67070: double_free: Calling "sqlite3_free" frees pointer "pWal->apWiData" which has already been freed. [Note: The source code implementation of the function has been overridden by a builtin model.]
#67068|       }
#67069|       WALTRACE(("WAL%p: closed\n", pWal));
#67070|->     sqlite3_free((void *)pWal->apWiData);
#67071|       sqlite3_free(pWal);
#67072|     }', true, 'Non-Issue', 'members of apWiData is set set to 0 after freeing. Second Freeing will be NOOP', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (430, 'sqlite-3.45.1-2.el10', 99, 'Error: BAD_FREE (CWE-590):
sqlite-src-3450100/shell.c:7841: address_free: "sqlite3_result_text" frees address of "pCur->zPath[pCur->nBase]".
# 7839|     switch( i ){
# 7840|       case FSDIR_COLUMN_NAME: {
# 7841|->       sqlite3_result_text(ctx, &pCur->zPath[pCur->nBase], -1, SQLITE_TRANSIENT);
# 7842|         break;
# 7843|       }', true, 'Non-Issue', 'when the SQLITE_TRANSIENT flag is set, the  array is not freed.
That is also this case.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (431, 'sqlite-3.45.1-2.el10', 100, 'Error: USE_AFTER_FREE (CWE-416):
sqlite-src-3450100/sqlite3.c:116587: assign: Assigning: "zBuf1" = "&zOut[nSql * 2LL + 1LL]".
sqlite-src-3450100/sqlite3.c:116588: assign: Assigning: "zBuf2" = "&zOut[nSql * 4LL + 2LL]".
sqlite-src-3450100/sqlite3.c:116643: freed_arg: "sqlite3_result_text" frees "zOut".
sqlite-src-3450100/sqlite3.c:116644: double_free: Calling "sqlite3DbFree" frees pointer "zOut" which has already been freed.
#116642|   
#116643|       sqlite3_result_text(pCtx, zOut, -1, SQLITE_TRANSIENT);
#116644|->     sqlite3DbFree(db, zOut);
#116645|     }else{
#116646|       rc = SQLITE_NOMEM;', true, 'Non-Issue', 'If SQLITE_TRANSIENT set no freeing takes place.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (434, 'sqlite-3.45.1-2.el10', 103, 'Error: USE_AFTER_FREE (CWE-416):
sqlite-src-3450100/sqlite3.c:232763: freed_arg: "sqlite3_result_text" frees "ctx.zOut".
sqlite-src-3450100/sqlite3.c:232765: double_free: Calling "sqlite3_free" frees pointer "ctx.zOut" which has already been freed. [Note: The source code implementation of the function has been overridden by a builtin model.]
#232763|         sqlite3_result_text(pCtx, (const char*)ctx.zOut, -1, SQLITE_TRANSIENT);
#232764|       }
#232765|->     sqlite3_free(ctx.zOut);
#232766|     }
#232767|     if( rc!=SQLITE_OK ){', true, 'Non-Issue', 'If SQLITE_TRANSIENT set no freeing takes place.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (435, 'sqlite-3.45.1-2.el10', 104, 'Error: USE_AFTER_FREE (CWE-416):
sqlite-src-3450100/sqlite3_analyzer.c:87915: freed_arg: "sqlite3VdbeTransferError" frees "p->zErrMsg".
sqlite-src-3450100/sqlite3_analyzer.c:87932: double_free: Calling "sqlite3DbFree" frees pointer "p->zErrMsg" which has already been freed.
#87930|   #endif
#87931|     if( p->zErrMsg ){
#87932|->     sqlite3DbFree(db, p->zErrMsg);
#87933|       p->zErrMsg = 0;
#87934|     }', true, 'Non-Issue', 'If SQLITE_TRANSIENT set no freeing takes place.
This is the case.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.170977');
INSERT INTO public.ground_truth VALUES (436, 'sysfsutils-2.1.1-7.el10', 1, 'Error: READLINK (CWE-170):
sysfsutils-2.1.1/lib/sysfs_utils.c:154: readlink_call: Passing size argument "256UL" implies readlink() can return up to "256UL" bytes.
sysfsutils-2.1.1/lib/sysfs_utils.c:154: readlink_assign: Assigning: "count" = readlink().
sysfsutils-2.1.1/lib/sysfs_utils.c:158: readlink: "linkpath[count]" is essentially buffer[sizeof(buffer)] which is an off-by-one error.
#  156|   		return -1;
#  157|   	else
#  158|-> 		linkpath[count] = ''\0'';
#  159|   	/*
#  160|   	 * Three cases here:', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-42557

', 'Assignment at line 158 (`linkpath[count] = ''
''`) can write one byte beyond the buffer''s last returned byte by `readlink`, assuming `count` equals `SYSFS_PATH_MAX` (256), constituting an off-by-one error and potential buffer overflow vulnerability.', '2025-11-18 16:18:56.19435');
INSERT INTO public.ground_truth VALUES (437, 'sysfsutils-2.1.1-7.el10', 2, 'Error: RESOURCE_LEAK (CWE-772):
sysfsutils-2.1.1/lib/sysfs_attr.c:507: alloc_fn: Storage is returned from allocation function "opendir".
sysfsutils-2.1.1/lib/sysfs_attr.c:507: var_assign: Assigning: "dir" = storage returned from "opendir(path)".
sysfsutils-2.1.1/lib/sysfs_attr.c:512: noescape: Resource "dir" is not freed or pointed-to in "readdir".
sysfsutils-2.1.1/lib/sysfs_attr.c:527: leaked_storage: Variable "dir" going out of scope leaks the storage it points to.
#  525|   				if (!dirlist) {
#  526|   					dbg_printf("Error creating list\n");
#  527|-> 					return NULL;
#  528|   				}
#  529|   			}', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-42557

', 'Resource leak confirmed: `dir` (allocated by `opendir()` at line 507) is not freed in the error path leading to `return NULL` at line 527, despite being freed only upon normal loop completion at line 535.', '2025-11-18 16:18:56.19435');
INSERT INTO public.ground_truth VALUES (438, 'sysfsutils-2.1.1-7.el10', 3, 'Error: RESOURCE_LEAK (CWE-772):
sysfsutils-2.1.1/lib/sysfs_device.c:242: alloc_fn: Storage is returned from allocation function "sysfs_read_dir_subdirs".
sysfsutils-2.1.1/lib/sysfs_device.c:242: var_assign: Assigning: "devlist" = storage returned from "sysfs_read_dir_subdirs(path)".
sysfsutils-2.1.1/lib/sysfs_device.c:251: leaked_storage: Variable "devlist" going out of scope leaks the storage it points to.
#  249|   						cur->path);
#  250|   				sysfs_close_device_tree(rootdev);
#  251|-> 				return NULL;
#  252|   			}
#  253|   			if (rootdev->children == NULL)', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-42557

', 'Memory allocated for `devlist` at line 242 is not explicitly freed before going out of scope at line 251, despite being potentially used only in a non-freening iteration (lines 244-249), aligning with CWE-772 (Resource Leak) description.', '2025-11-18 16:18:56.19435');
INSERT INTO public.ground_truth VALUES (439, 'sysfsutils-2.1.1-7.el10', 4, 'Error: OVERRUN (CWE-119):
sysfsutils-2.1.1/lib/sysfs_utils.c:154: identity_transfer: Passing "256UL" as argument 3 to function "readlink", which returns that argument. [Note: The source code implementation of the function has been overridden by a builtin model.]
sysfsutils-2.1.1/lib/sysfs_utils.c:154: assignment: Assigning: "count" = "readlink(path, linkpath, 256UL)". The value of "count" is now 256.
sysfsutils-2.1.1/lib/sysfs_utils.c:158: overrun-local: Overrunning array "linkpath" of 256 bytes at byte offset 256 using index "count" (which evaluates to 256).
#  156|   		return -1;
#  157|   	else
#  158|-> 		linkpath[count] = ''\0'';
#  159|   	/*
#  160|   	 * Three cases here:', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-42557

', 'Assignment of `count` with `readlink`''s return value (line 154) leads to out-of-bounds access of `linkpath` array (line 158) when `count` equals 256, as the last valid index for a 256-element array is 255, directly correlating with the reported ''overrun-local'' vulnerability.', '2025-11-18 16:18:56.19435');
INSERT INTO public.ground_truth VALUES (440, 'sysfsutils-2.1.1-7.el10', 5, 'Error: RESOURCE_LEAK (CWE-772):
sysfsutils-2.1.1/lib/sysfs_attr.c:555: alloc_fn: Storage is returned from allocation function "opendir".
sysfsutils-2.1.1/lib/sysfs_attr.c:555: var_assign: Assigning: "dir" = storage returned from "opendir(path)".
sysfsutils-2.1.1/lib/sysfs_attr.c:560: noescape: Resource "dir" is not freed or pointed-to in "readdir".
sysfsutils-2.1.1/lib/sysfs_attr.c:576: leaked_storage: Variable "dir" going out of scope leaks the storage it points to.
#  574|   				if (!alist) {
#  575|   					dbg_printf("Error creating list\n");
#  576|-> 					return NULL;
#  577|   				}
#  578|   			}', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-42557

', 'Resource leak confirmed as `dir` (allocated by `opendir()` at line 555) is not freed in the error path when `alist` is `NULL` (lines 574-576), unlike the successful path where `closedir(dir)` is called (line 582).', '2025-11-18 16:18:56.19435');
INSERT INTO public.ground_truth VALUES (441, 'sysfsutils-2.1.1-7.el10', 6, 'Error: RESOURCE_LEAK (CWE-772):
sysfsutils-2.1.1/lib/sysfs_attr.c:468: alloc_fn: Storage is returned from allocation function "sysfs_open_device_path".
sysfsutils-2.1.1/lib/sysfs_attr.c:468: var_assign: Assigning: "dev" = storage returned from "sysfs_open_device_path(path)".
sysfsutils-2.1.1/lib/sysfs_attr.c:473: leaked_storage: Variable "dev" going out of scope leaks the storage it points to.
#  471|   	if (!dir) {
#  472|   		dbg_printf("Error opening directory %s\n", path);
#  473|-> 		return NULL;
#  474|   	}
#  475|   	while ((dirent = readdir(dir)) != NULL) {', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-42557

', 'Memory allocated for `dev` at line 468 is not freed before returning `NULL` at line 473 when `dir` is `NULL`, directly indicating a memory leak in the error handling path, with no evident mitigating memory management in the provided code context.', '2025-11-18 16:18:56.19435');
INSERT INTO public.ground_truth VALUES (462, 'texinfo-7.1-2.el10', 17, 'Error: USE_AFTER_FREE (CWE-416):
texinfo-7.1/info/info.c:1048: freed_arg: "add_initial_nodes" frees "error".
texinfo-7.1/info/info.c:1085: pass_freed_arg: Passing freed pointer "error" as an argument to "info_session".
# 1083|       }
# 1084|       
# 1085|->   info_session (ref_list, all_matches_p ? user_filename : 0, error);
# 1086|     close_info_session ();
# 1087|     exit (0);', true, 'Non-Issue', 'add_initial_nodes() either frees "error" on line 427 but allocates it right again with xasprintf (wrapper to asprintf) on the following line 428, or frees "error" and sets it to NULL on lines 525, 539, 570', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.203498');
INSERT INTO public.ground_truth VALUES (442, 'sysfsutils-2.1.1-7.el10', 7, 'Error: RESOURCE_LEAK (CWE-772):
sysfsutils-2.1.1/lib/sysfs_driver.c:245: alloc_fn: Storage is returned from allocation function "sysfs_open_device".
sysfsutils-2.1.1/lib/sysfs_driver.c:245: var_assign: Assigning: "dev" = storage returned from "sysfs_open_device(drv->bus, ln)".
sysfsutils-2.1.1/lib/sysfs_driver.c:258: leaked_storage: Variable "dev" going out of scope leaks the storage it points to.
#  256|   					dbg_printf("Error creating device list\n");
#  257|   					sysfs_close_list(linklist);
#  258|-> 					return NULL;
#  259|   				}
#  260|   			}', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-42557

', 'Variable `dev` is allocated at line 245 but not freed before returning `NULL` at line 258 in the error handling block, indicating a potential resource leak, as only `linklist` is closed (line 257) and not `dev`.', '2025-11-18 16:18:56.19435');
INSERT INTO public.ground_truth VALUES (443, 'sysfsutils-2.1.1-7.el10', 8, 'Error: RESOURCE_LEAK (CWE-772):
sysfsutils-2.1.1/lib/sysfs_device.c:242: alloc_fn: Storage is returned from allocation function "sysfs_read_dir_subdirs".
sysfsutils-2.1.1/lib/sysfs_device.c:242: var_assign: Assigning: "devlist" = storage returned from "sysfs_read_dir_subdirs(path)".
sysfsutils-2.1.1/lib/sysfs_device.c:261: leaked_storage: Variable "devlist" going out of scope leaks the storage it points to.
#  259|   	}
#  260|   
#  261|-> 	return rootdev;
#  262|   }
#  263|', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-42557

', 'Memory allocated for `devlist` at line 242 is not explicitly freed or transferred before going out of scope at line 261, indicating a potential RESOURCE_LEAK (CWE-772) with no evident null check or external memory handling to refute the leak.', '2025-11-18 16:18:56.19435');
INSERT INTO public.ground_truth VALUES (444, 'sysfsutils-2.1.1-7.el10', 9, 'Error: INTEGER_OVERFLOW (CWE-190):
sysfsutils-2.1.1/lib/sysfs_attr.c:167: tainted_data_return: Called function "read(fd, fbuf, pgsize)", and a possible return value may be less than zero.
sysfsutils-2.1.1/lib/sysfs_attr.c:167: assign: Assigning: "length" = "read(fd, fbuf, pgsize)".
sysfsutils-2.1.1/lib/sysfs_attr.c:185: overflow: The expression "length + 1L" is considered to have possibly overflowed.
sysfsutils-2.1.1/lib/sysfs_attr.c:185: overflow_sink: "length + 1L", which might have overflowed, is passed to "realloc(fbuf, length + 1L)".
#  183|   	sysattr->len = length;
#  184|   	close(fd);
#  185|-> 	vbuf = (char *)realloc(fbuf, length+1);
#  186|   	if (!vbuf) {
#  187|   		dbg_printf("realloc failed\n");', true, 'Non-Issue', 'Line 168 ensures that "length" can''t be less than zero on line 185.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.19435');
INSERT INTO public.ground_truth VALUES (445, 'sysfsutils-2.1.1-7.el10', 10, 'Error: RESOURCE_LEAK (CWE-772):
sysfsutils-2.1.1/lib/sysfs_attr.c:398: alloc_fn: Storage is returned from allocation function "opendir".
sysfsutils-2.1.1/lib/sysfs_attr.c:398: var_assign: Assigning: "dir" = storage returned from "opendir(path)".
sysfsutils-2.1.1/lib/sysfs_attr.c:403: noescape: Resource "dir" is not freed or pointed-to in "readdir".
sysfsutils-2.1.1/lib/sysfs_attr.c:418: leaked_storage: Variable "dir" going out of scope leaks the storage it points to.
#  416|   				if (!linklist) {
#  417|   					dbg_printf("Error creating list\n");
#  418|-> 					return NULL;
#  419|   				}
#  420|   			}', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-42557

', 'Resource leak confirmed: `dir` (allocated by `opendir()` at line 398) is not freed in the error path where `linklist` is NULL (line 418), as `closedir(dir)` (line 426) is bypassed upon immediate return.', '2025-11-18 16:18:56.19435');
INSERT INTO public.ground_truth VALUES (446, 'texinfo-7.1-2.el10', 1, 'Error: USE_AFTER_FREE (CWE-416):
texinfo-7.1/info/session.c:2903: freed_arg: "info_follow_menus" frees "error".
texinfo-7.1/info/session.c:2906: pass_freed_arg: Passing freed pointer "error" as an argument to "show_error_node".
# 2904|             info_set_node_of_window (window, node);
# 2905|             if (error)
# 2906|->             show_error_node (error);
# 2907|           }
# 2908|', true, 'Non-Issue', 'info_follow_menus() frees "error" on lines 2769, 2791, 2815 but allocates it right again with xasprintf (wrapper to asprintf) on the following lines 2770, 2792, 2816', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.203498');
INSERT INTO public.ground_truth VALUES (447, 'texinfo-7.1-2.el10', 2, 'Error: INTEGER_OVERFLOW (CWE-190):
texinfo-7.1/info/makedoc.c:299: tainted_data_argument: The check "offset < file_size - decl_len" contains the tainted expression "file_size - decl_len" which causes "offset" to be considered tainted.
texinfo-7.1/info/makedoc.c:309: overflow: The expression "offset += decl_len" is deemed overflowed because at least one of its arguments has overflowed.
texinfo-7.1/info/makedoc.c:310: assign: Assigning: "point" = "offset".
texinfo-7.1/info/makedoc.c:347: assign: Assigning: "offset" = "point".
texinfo-7.1/info/makedoc.c:356: overflow: The expression "offset - line_start" is deemed underflowed because at least one of its arguments has underflowed.
texinfo-7.1/info/makedoc.c:356: overflow: The expression "1L + (offset - line_start)" is deemed underflowed because at least one of its arguments has underflowed.
texinfo-7.1/info/makedoc.c:356: overflow_sink: "1L + (offset - line_start)", which might have underflowed, is passed to "xmalloc(1L + (offset - line_start))". [Note: The source code implementation of the function has been overridden by a builtin model.]
#  354|           char *tag_name;
#  355|   
#  356|->         tag_name = xmalloc (1 + (offset - line_start));
#  357|           strncpy (tag_name, buffer + line_start, offset - line_start);
#  358|           tag_name[offset - line_start] = ''\0'';', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-43596

This is a utility program used just during the build of the package, it''s not shipped to customers in any way', NULL, '2025-11-18 16:18:56.203498');
INSERT INTO public.ground_truth VALUES (448, 'texinfo-7.1-2.el10', 3, 'Error: RESOURCE_LEAK (CWE-772):
texinfo-7.1/info/filesys.c:155: alloc_fn: Storage is returned from allocation function "tilde_expand_word".
texinfo-7.1/info/filesys.c:155: var_assign: Assigning: "expanded_dirname" = storage returned from "tilde_expand_word(dirname)".
texinfo-7.1/info/filesys.c:156: var_assign: Assigning: "dirname" = "expanded_dirname".
texinfo-7.1/info/filesys.c:157: leaked_storage: Variable "expanded_dirname" going out of scope leaks the storage it points to.
texinfo-7.1/info/filesys.c:159: noescape: Resource "dirname" is not freed or pointed-to in "info_add_extension".
texinfo-7.1/info/filesys.c:169: leaked_storage: Variable "dirname" going out of scope leaks the storage it points to.
#  167|                 xasprintf (&s, "%s%s", "./", with_extension);
#  168|                 free (with_extension);
#  169|->               return s;
#  170|               }
#  171|             else', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-43595

', NULL, '2025-11-18 16:18:56.203498');
INSERT INTO public.ground_truth VALUES (449, 'texinfo-7.1-2.el10', 4, 'Error: USE_AFTER_FREE (CWE-416):
texinfo-7.1/info/session.c:1967: freed_arg: "window_delete_window" frees "window".
texinfo-7.1/info/session.c:1970: pass_freed_arg: Passing freed pointer "window" as an argument to "echo_area_inform_of_deleted_window".
# 1968|   
# 1969|         if (echo_area_is_active)
# 1970|->         echo_area_inform_of_deleted_window (window);
# 1971|       }
# 1972|   }', true, 'Non-Issue', 'This is intentional. "window" is free''d, but not NULLed and echo_area_inform_of_deleted_window() just works with memory address, not the content.', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.203498');
INSERT INTO public.ground_truth VALUES (450, 'texinfo-7.1-2.el10', 5, 'Error: OVERRUN (CWE-119):
texinfo-7.1/install-info/install-info.c:1611: alloc_strlen: Allocating insufficient memory for the terminating null of the string. [Note: The source code implementation of the function has been overridden by a builtin model.]
# 1609|   
# 1610|     ptr++;
# 1611|->   *description = xmalloc (strlen (entry));
# 1612|     (*description)[0] = ''\0'';
# 1613|', true, 'Non-Issue', 'There''s more than sufficient memory as the "description" stores just a substring of entry (entry is in format "NAME. DESCRIPTION")', 'Insufficient memory allocation at line 1611 (`xmalloc (strlen (entry))`) fails to account for the null terminator, leading to a buffer overflow by one byte when writing `(*description)[0] = ''\0'';` at line 1612, directly correlating with CWE-119.', '2025-11-18 16:18:56.203498');
INSERT INTO public.ground_truth VALUES (453, 'texinfo-7.1-2.el10', 8, 'Error: RESOURCE_LEAK (CWE-772):
texinfo-7.1/install-info/install-info.c:750: alloc_fn: Storage is returned from allocation function "fopen".
texinfo-7.1/install-info/install-info.c:750: var_assign: Assigning: "f" = storage returned from "fopen(*opened_filename, "r")".
texinfo-7.1/install-info/install-info.c:753: noescape: Resource "f" is not freed or pointed-to in "fread". [Note: The source code implementation of the function has been overridden by a builtin model.]
texinfo-7.1/install-info/install-info.c:856: noescape: Resource "f" is not freed or pointed-to in "fseek".
texinfo-7.1/install-info/install-info.c:857: leaked_storage: Variable "f" going out of scope leaks the storage it points to.
#  855|         /* Seek back over the magic bytes.  */
#  856|         if (fseek (f, 0, 0) < 0)
#  857|->         return 0;
#  858|   #endif
#  859|       }', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-43595

', NULL, '2025-11-18 16:18:56.203498');
INSERT INTO public.ground_truth VALUES (454, 'texinfo-7.1-2.el10', 9, 'Error: USE_AFTER_FREE (CWE-416):
texinfo-7.1/info/info.c:499: freed_arg: "info_follow_menus" frees "*error".
texinfo-7.1/info/info.c:539: double_free: Calling "free" frees pointer "*error" which has already been freed.
#  537|                                                         ref_list[0]->nodename,
#  538|                                                         0);
#  539|->           free (*error); *error = 0;
#  540|             node_via_menus = info_follow_menus (initial_node, argv, error, 0);
#  541|             if (node_via_menus)', true, 'Non-Issue', 'info_follow_menus() frees "error" on lines 2769, 2791, 2815 but allocates it right again with xasprintf (wrapper to asprintf) on the following lines 2770, 2792, 2816', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.203498');
INSERT INTO public.ground_truth VALUES (455, 'texinfo-7.1-2.el10', 10, 'Error: INTEGER_OVERFLOW (CWE-190):
texinfo-7.1/info/makedoc.c:299: tainted_data_argument: The check "offset < file_size - decl_len" contains the tainted expression "file_size - decl_len" which causes "offset" to be considered tainted.
texinfo-7.1/info/makedoc.c:309: overflow: The expression "offset += decl_len" is deemed overflowed because at least one of its arguments has overflowed.
texinfo-7.1/info/makedoc.c:310: assign: Assigning: "point" = "offset".
texinfo-7.1/info/makedoc.c:347: assign: Assigning: "offset" = "point".
texinfo-7.1/info/makedoc.c:349: overflow: The expression "offset - point" is deemed underflowed because at least one of its arguments has underflowed.
texinfo-7.1/info/makedoc.c:349: overflow_sink: "offset - point", which might have underflowed, is passed to "strncpy(func, buffer + point, offset - point)". [Note: The source code implementation of the function has been overridden by a builtin model.]
#  347|         for (offset = point; buffer[offset] != '',''; offset++);
#  348|         func = xmalloc (1 + (offset - point));
#  349|->       strncpy (func, buffer + point, offset - point);
#  350|         func[offset - point] = ''\0'';
#  351|', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-43596

This is a utility program used just during the build of the package, it''s not shipped to customers in any way', NULL, '2025-11-18 16:18:56.203498');
INSERT INTO public.ground_truth VALUES (456, 'texinfo-7.1-2.el10', 11, 'Error: USE_AFTER_FREE (CWE-416):
texinfo-7.1/info/info.c:1048: freed_arg: "add_initial_nodes" frees "error".
texinfo-7.1/info/info.c:1079: pass_freed_arg: Passing freed pointer "error" as an argument to "info_error".
# 1077|         if (error)
# 1078|           {
# 1079|->           info_error ("%s", error);
# 1080|             exit (1);
# 1081|           }', true, 'Non-Issue', 'add_initial_nodes() either frees "error" on line 427 but allocates it right again with xasprintf (wrapper to asprintf) on the following line 428, or frees "error" and sets it to NULL on lines 525, 539, 570', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.203498');
INSERT INTO public.ground_truth VALUES (457, 'texinfo-7.1-2.el10', 12, 'Error: RESOURCE_LEAK (CWE-772):
texinfo-7.1/install-info/install-info.c:662: alloc_fn: Storage is returned from allocation function "fopen".
texinfo-7.1/install-info/install-info.c:662: var_assign: Assigning: "f" = storage returned from "fopen(*opened_filename, "r")".
texinfo-7.1/install-info/install-info.c:735: noescape: Resource "f" is not freed or pointed-to in "fread". [Note: The source code implementation of the function has been overridden by a builtin model.]
texinfo-7.1/install-info/install-info.c:741: noescape: Resource "f" is not freed or pointed-to in "feof".
texinfo-7.1/install-info/install-info.c:760: leaked_storage: Variable "f" going out of scope leaks the storage it points to.
#  758|           }
#  759|         errno = 0;
#  760|->       return 0; /* unknown error */
#  761|       }
#  762|', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-43595

', NULL, '2025-11-18 16:18:56.203498');
INSERT INTO public.ground_truth VALUES (458, 'texinfo-7.1-2.el10', 13, 'Error: USE_AFTER_FREE (CWE-416):
texinfo-7.1/info/session.c:2903: freed_arg: "info_follow_menus" frees "error".
texinfo-7.1/info/session.c:2906: deref_arg: Calling "show_error_node" dereferences freed pointer "error".
# 2904|             info_set_node_of_window (window, node);
# 2905|             if (error)
# 2906|->             show_error_node (error);
# 2907|           }
# 2908|', true, 'Non-Issue', 'info_follow_menus() frees "error" on lines 2769, 2791, 2815 but allocates it right again with xasprintf (wrapper to asprintf) on the following lines 2770, 2792, 2816', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.203498');
INSERT INTO public.ground_truth VALUES (469, 'texinfo-7.1-2.el10', 24, 'Error: UNINIT (CWE-457):
texinfo-7.1/info/session.c:1132: var_decl: Declaring variable "iter" without initializer.
texinfo-7.1/info/session.c:1137: uninit_use: Using uninitialized value "iter.cur.wc_valid".
# 1135|     mbi_avail (iter);
# 1136|   
# 1137|->   return mbi_cur (iter).wc_valid && iswalnum (mbi_cur (iter).wc);
# 1138|   }
# 1139|', true, 'Issue', '"iter" is inicialized with mbi_init() on line 1133', 'Node memory allocated by info_node_of_tag_fast may be handled by the program''s broader memory management strategy, not explicitly shown in the provided snippet, particularly in the funexit path (Ctrl+G) where typical free operations are absent.', '2025-11-18 16:18:56.203498');
INSERT INTO public.ground_truth VALUES (470, 'texinfo-7.1-2.el10', 25, 'Error: RESOURCE_LEAK (CWE-772):
texinfo-7.1/info/session.c:4189: alloc_fn: Storage is returned from allocation function "info_node_of_tag_fast".
texinfo-7.1/info/session.c:4189: var_assign: Assigning: "node" = storage returned from "info_node_of_tag_fast(file_buffer, &tag)".
texinfo-7.1/info/session.c:4226: leaked_storage: Variable "node" going out of scope leaks the storage it points to.
# 4224|   
# 4225|   funexit:
# 4226|->   return -1;
# 4227|   }
# 4228|', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-43595

', NULL, '2025-11-18 16:18:56.203498');
INSERT INTO public.ground_truth VALUES (471, 'texinfo-7.1-2.el10', 26, 'Error: RESOURCE_LEAK (CWE-772):
texinfo-7.1/install-info/install-info.c:828: alloc_fn: Storage is returned from allocation function "concat".
texinfo-7.1/install-info/install-info.c:828: var_assign: Assigning: "command" = storage returned from "concat(*compression_program, " -d", "")".
texinfo-7.1/install-info/install-info.c:832: leaked_storage: Variable "command" going out of scope leaks the storage it points to.
#  830|   
#  831|         if (fclose (f) < 0)
#  832|->         return 0;
#  833|         f2 = freopen (*opened_filename, FOPEN_RBIN, stdin);
#  834|         if (!f)', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-43595

', NULL, '2025-11-18 16:18:56.203498');
INSERT INTO public.ground_truth VALUES (473, 'texinfo-7.1-2.el10', 28, 'Error: RESOURCE_LEAK (CWE-772):
texinfo-7.1/install-info/install-info.c:750: alloc_fn: Storage is returned from allocation function "fopen".
texinfo-7.1/install-info/install-info.c:750: var_assign: Assigning: "f" = storage returned from "fopen(*opened_filename, "r")".
texinfo-7.1/install-info/install-info.c:753: noescape: Resource "f" is not freed or pointed-to in "fread". [Note: The source code implementation of the function has been overridden by a builtin model.]
texinfo-7.1/install-info/install-info.c:755: leaked_storage: Variable "f" going out of scope leaks the storage it points to.
#  753|                 nread = fread (data, sizeof (data), 1, f);
#  754|                 if (nread == 0)
#  755|->                 return 0;
#  756|                 goto determine_file_type; /* success */
#  757|               }', false, 'Non-Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-43595

', NULL, '2025-11-18 16:18:56.203498');
INSERT INTO public.ground_truth VALUES (474, 'texinfo-7.1-2.el10', 29, '', false, 'Non-Issue', NULL, NULL, '2025-11-18 16:18:56.203498');
INSERT INTO public.ground_truth VALUES (475, 'texinfo-7.1-2.el10', 30, 'Error: UNINIT (CWE-457):
texinfo-7.1/info/session.c:1121: var_decl: Declaring variable "iter" without initializer.
texinfo-7.1/info/session.c:1125: uninit_use: Using uninitialized value "iter.cur.wc".
# 1123|   	    win->node->nodelen - point);
# 1124|     mbi_avail (iter);
# 1125|->   return mbi_cur (iter).wc_valid && mbi_cur (iter).wc == ''\n'';
# 1126|   }
# 1127|', true, 'Non-Issue', '"iter" is inicialized with mbi_init() on line 1122', 'Variable ''iter'' is initialized with `mbi_init()` on line 513 before its first use in the loop, ensuring ''iter.cur.bytes'', ''iter.cur.wc'', and ''iter.cur.wc_valid'' are not uninitialized when used in `mbi_copy()` on line 519.', '2025-11-18 16:18:56.203498');
INSERT INTO public.ground_truth VALUES (476, 'texinfo-7.1-2.el10', 31, 'Error: USE_AFTER_FREE (CWE-416):
texinfo-7.1/info/info.c:499: freed_arg: "info_follow_menus" frees "*error".
texinfo-7.1/info/info.c:525: double_free: Calling "free" frees pointer "*error" which has already been freed.
#  523|                   {
#  524|                     argv += argc; argc = 0;
#  525|->                   free (*error); *error = 0;
#  526|   
#  527|                     info_reference_free (ref_list[0]);', true, 'Issue', 'info_follow_menus() frees "error" on lines 2769, 2791, 2815 but allocates it right again with xasprintf (wrapper to asprintf) on the following lines 2770, 2792, 2816', 'Variable ''err'' is declared without an initializer (line 1839) and its uninitialized value is used at line 1885 if ''sprintf'' returns a non-error value, directly correlating with the CVE''s described vulnerability of using an uninitialized value.', '2025-11-18 16:18:56.203498');
INSERT INTO public.ground_truth VALUES (477, 'texinfo-7.1-2.el10', 32, 'Error: UNINIT (CWE-457):
texinfo-7.1/info/display.c:492: var_decl: Declaring variable "iter" without initializer.
texinfo-7.1/info/display.c:519: uninit_use_in_call: Using uninitialized value "iter.cur.bytes" when calling "mbiter_multi_copy".
texinfo-7.1/info/display.c:519: uninit_use_in_call: Using uninitialized value "iter.cur.wc" when calling "mbiter_multi_copy".
texinfo-7.1/info/display.c:519: uninit_use_in_call: Using uninitialized value "iter.cur.wc_valid" when calling "mbiter_multi_copy".
#  517|       {
#  518|         int delim;
#  519|->       mbi_copy (&bol_iter, &iter);
#  520|         bol_ref_index = ref_index;
#  521|         bol_match_index = match_index;', true, 'Issue', '"iter" is inicialized with mbi_init() on line 513', 'Crafted file inputs can potentially induce overflow/underflow in `offset` calculations (lines 309, 347, 348), leading to incorrect `xmalloc` memory allocation, as `offset` and `point` are influenced by file content parsing, directly impacting memory safety.', '2025-11-18 16:18:56.203498');
INSERT INTO public.ground_truth VALUES (478, 'texinfo-7.1-2.el10', 33, 'Error: UNINIT (CWE-457):
texinfo-7.1/install-info/install-info.c:1839: var_decl: Declaring variable "err" without initializer.
texinfo-7.1/install-info/install-info.c:1885: uninit_use: Using uninitialized value "err".
# 1883|             if (sprintf (opt, "--regex=%s", regex) == -1)
# 1884|               err = 1;
# 1885|->           if (!err)
# 1886|               err = argz_add (&argz, &argz_len, opt);
# 1887|             free (opt); opt = NULL;', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-43595

', NULL, '2025-11-18 16:18:56.203498');
INSERT INTO public.ground_truth VALUES (479, 'texinfo-7.1-2.el10', 34, 'Error: INTEGER_OVERFLOW (CWE-190):
texinfo-7.1/info/makedoc.c:299: tainted_data_argument: The check "offset < file_size - decl_len" contains the tainted expression "file_size - decl_len" which causes "offset" to be considered tainted.
texinfo-7.1/info/makedoc.c:309: overflow: The expression "offset += decl_len" is deemed overflowed because at least one of its arguments has overflowed.
texinfo-7.1/info/makedoc.c:310: assign: Assigning: "point" = "offset".
texinfo-7.1/info/makedoc.c:347: assign: Assigning: "offset" = "point".
texinfo-7.1/info/makedoc.c:348: overflow: The expression "offset - point" is deemed underflowed because at least one of its arguments has underflowed.
texinfo-7.1/info/makedoc.c:348: overflow: The expression "1L + (offset - point)" is deemed underflowed because at least one of its arguments has underflowed.
texinfo-7.1/info/makedoc.c:348: overflow_sink: "1L + (offset - point)", which might have underflowed, is passed to "xmalloc(1L + (offset - point))". [Note: The source code implementation of the function has been overridden by a builtin model.]
#  346|         /* Now looking at name of function.  Get it. */
#  347|         for (offset = point; buffer[offset] != '',''; offset++);
#  348|->       func = xmalloc (1 + (offset - point));
#  349|         strncpy (func, buffer + point, offset - point);
#  350|         func[offset - point] = ''\0'';', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-43596

This is a utility program used just during the build of the package, it''s not shipped to customers in any way', NULL, '2025-11-18 16:18:56.203498');
INSERT INTO public.ground_truth VALUES (480, 'texinfo-7.1-2.el10', 35, 'Error: RESOURCE_LEAK (CWE-772):
texinfo-7.1/info/filesys.c:155: alloc_fn: Storage is returned from allocation function "tilde_expand_word".
texinfo-7.1/info/filesys.c:155: var_assign: Assigning: "expanded_dirname" = storage returned from "tilde_expand_word(dirname)".
texinfo-7.1/info/filesys.c:156: var_assign: Assigning: "dirname" = "expanded_dirname".
texinfo-7.1/info/filesys.c:157: leaked_storage: Variable "expanded_dirname" going out of scope leaks the storage it points to.
texinfo-7.1/info/filesys.c:159: noescape: Resource "dirname" is not freed or pointed-to in "info_add_extension".
texinfo-7.1/info/filesys.c:172: leaked_storage: Variable "dirname" going out of scope leaks the storage it points to.
#  170|               }
#  171|             else
#  172|->             return with_extension;
#  173|           }
#  174|       }', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-43595

', NULL, '2025-11-18 16:18:56.203498');
INSERT INTO public.ground_truth VALUES (481, 'texinfo-7.1-2.el10', 36, 'Error: RESOURCE_LEAK (CWE-772):
texinfo-7.1/info/filesys.c:155: alloc_fn: Storage is returned from allocation function "tilde_expand_word".
texinfo-7.1/info/filesys.c:155: var_assign: Assigning: "expanded_dirname" = storage returned from "tilde_expand_word(dirname)".
texinfo-7.1/info/filesys.c:156: var_assign: Assigning: "dirname" = "expanded_dirname".
texinfo-7.1/info/filesys.c:157: leaked_storage: Variable "expanded_dirname" going out of scope leaks the storage it points to.
texinfo-7.1/info/filesys.c:159: noescape: Resource "dirname" is not freed or pointed-to in "info_add_extension".
texinfo-7.1/info/filesys.c:174: leaked_storage: Variable "dirname" going out of scope leaks the storage it points to.
#  172|               return with_extension;
#  173|           }
#  174|->     }
#  175|     return NULL;
#  176|   }', false, 'Non-Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-43595

', NULL, '2025-11-18 16:18:56.203498');
INSERT INTO public.ground_truth VALUES (482, 'texinfo-7.1-2.el10', 37, 'Error: INTEGER_OVERFLOW (CWE-190):
texinfo-7.1/info/makedoc.c:299: tainted_data_argument: The check "offset < file_size - decl_len" contains the tainted expression "file_size - decl_len" which causes "offset" to be considered tainted.
texinfo-7.1/info/makedoc.c:309: overflow: The expression "offset += decl_len" is deemed overflowed because at least one of its arguments has overflowed.
texinfo-7.1/info/makedoc.c:310: assign: Assigning: "point" = "offset".
texinfo-7.1/info/makedoc.c:347: assign: Assigning: "offset" = "point".
texinfo-7.1/info/makedoc.c:357: overflow: The expression "offset - line_start" is deemed underflowed because at least one of its arguments has underflowed.
texinfo-7.1/info/makedoc.c:357: overflow_sink: "offset - line_start", which might have underflowed, is passed to "strncpy(tag_name, buffer + line_start, offset - line_start)". [Note: The source code implementation of the function has been overridden by a builtin model.]
#  355|   
#  356|           tag_name = xmalloc (1 + (offset - line_start));
#  357|->         strncpy (tag_name, buffer + line_start, offset - line_start);
#  358|           tag_name[offset - line_start] = ''\0'';
#  359|           add_tag_to_block (block, tag_name, line_number, point);', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-43596

This is a utility program used just during the build of the package, it''s not shipped to customers in any way', NULL, '2025-11-18 16:18:56.203498');
INSERT INTO public.ground_truth VALUES (483, 'texinfo-7.1-2.el10', 38, 'Error: RESOURCE_LEAK (CWE-772):
texinfo-7.1/info/session.c:3660: alloc_fn: Storage is returned from allocation function "info_read_in_echo_area".
texinfo-7.1/info/session.c:3660: var_assign: Assigning: "line" = storage returned from "info_read_in_echo_area(dcgettext(NULL, "Find file: ", 5))".
texinfo-7.1/info/session.c:3684: leaked_storage: Variable "line" going out of scope leaks the storage it points to.
# 3682|         free (line);
# 3683|       }
# 3684|-> }
# 3685|   _x000c_
# 3686|   /* **************************************************************** */', true, 'Non-Issue', 'info_read_in_echo_area() reads string and returns pointer to it or NULL. If "line" is NULL the function returns on line 3664, otherwise "line" is free''d on line 3682', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.203498');
INSERT INTO public.ground_truth VALUES (558, 'unzip-6.0-63.el10', 1, 'Error: BUFFER_SIZE (CWE-474):
unzip60/envargs.c:121: overlapping_buffer: The source buffer "argstart + 1" potentially overlaps with the destination buffer "argstart", which results in undefined behavior for "strcpy".
unzip60/envargs.c:121: remediation: Replace "strcpy(dest, src)" with "memmove(dest, src, strlen(src)+1)".
#  119|               /* remove escape characters */
#  120|               while ((argstart = MBSCHR(argstart, ''\\'')) != (char *)NULL) {
#  121|->                 strcpy(argstart, argstart + 1);
#  122|                   if (*argstart)
#  123|                       ++argstart;', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-44659

bug', 'The `strcpy` call at `unzip60/envargs.c:121` inherently introduces undefined behavior due to overlapping source (`argstart + 1`) and destination (`argstart`) buffers, as explicitly warned against in the C standard (C11 §7.24.2.3p2), with no mitigating implementation provided in the given code context.', '2025-11-18 16:18:56.240797');
INSERT INTO public.ground_truth VALUES (559, 'unzip-6.0-63.el10', 2, 'Error: OVERRUN (CWE-119):
unzip60/inflate.c:1608: cond_const: Checking "j <= 16U" implies that "j" is 17 on the false branch.
unzip60/inflate.c:1613: assignment: Assigning: "*m" = "j". The value of "*m" is now 17.
unzip60/inflate.c:1614: assignment: Assigning: "i" = "16U".
unzip60/inflate.c:1614: decr: Decrementing "i". The value of "i" is now 15.
unzip60/inflate.c:1614: cond_at_least: Checking "i" implies that "i" is at least 1 on the true branch.
unzip60/inflate.c:1618: cond_at_least: Checking "*m > i" implies that "g" and "i" are at least 17 on the false branch.
unzip60/inflate.c:1628: overrun-local: Overrunning array "c" of 17 4-byte elements at element index 17 (byte offset 71) using index "i" (which evaluates to 17).
# 1626|     if ((y -= c[i]) < 0)
# 1627|       return 2;
# 1628|->   c[i] += y;
# 1629|   
# 1630|', true, 'Non-Issue', 'C is an array of size BMAX+1, i is between 1 and BMAX (inclusive)', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.240797');
INSERT INTO public.ground_truth VALUES (560, 'unzip-6.0-63.el10', 3, 'Error: INTEGER_OVERFLOW (CWE-125):
unzip60/explode.c:406: tainted_data_return: The value returned by "readbyte()" is considered tainted.
unzip60/explode.c:406: overflow: The expression "(ulg)((G.incnt-- > 0) ? (int)*G.inptr++ : readbyte()) << k" might be negative, but is used in a context that treats it as unsigned.
unzip60/explode.c:406: overflow: The expression "b |= (ulg)((G.incnt-- > 0) ? (int)*G.inptr++ : readbyte()) << k" is deemed underflowed because at least one of its arguments has underflowed.
unzip60/explode.c:414: overflow: The expression "b >>= 8" is deemed underflowed because at least one of its arguments has underflowed.
unzip60/explode.c:418: overflow: The expression "b >>= 1" is deemed underflowed because at least one of its arguments has underflowed.
unzip60/explode.c:420: overflow: The expression "(unsigned int)b & mdl" is deemed underflowed because at least one of its arguments has underflowed.
unzip60/explode.c:420: assign: Assigning: "d" = "(unsigned int)b & mdl".
unzip60/explode.c:444: overflow: The expression "d &= 0xffffL" is deemed underflowed because at least one of its arguments has underflowed.
unzip60/explode.c:451: overflow: The expression "d += e" is deemed underflowed because at least one of its arguments has underflowed.
unzip60/explode.c:444: overflow: The expression "d &= 0xffffL" is deemed underflowed because at least one of its arguments has underflowed.
unzip60/explode.c:464: deref_overflow: "d", which might have underflowed, is passed to "G.area.Slide[d++]".
#  462|   #endif /* !NOMEMCPY */
#  463|               do {
#  464|->               redirSlide[w++] = redirSlide[d++];
#  465|               } while (--e);
#  466|           if (w == wszimpl)', true, 'Non-Issue', 'low level bit operations', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.240797');
INSERT INTO public.ground_truth VALUES (561, 'unzip-6.0-63.el10', 4, 'Error: INTEGER_OVERFLOW (CWE-190):
unzip60/explode.c:406: tainted_data_return: The value returned by "readbyte()" is considered tainted.
unzip60/explode.c:406: overflow: The expression "(ulg)((G.incnt-- > 0) ? (int)*G.inptr++ : readbyte()) << k" might be negative, but is used in a context that treats it as unsigned.
unzip60/explode.c:406: overflow: The expression "b |= (ulg)((G.incnt-- > 0) ? (int)*G.inptr++ : readbyte()) << k" is deemed underflowed because at least one of its arguments has underflowed.
unzip60/explode.c:414: overflow: The expression "b >>= 8" is deemed underflowed because at least one of its arguments has underflowed.
unzip60/explode.c:418: overflow: The expression "b >>= 1" is deemed underflowed because at least one of its arguments has underflowed.
unzip60/explode.c:420: overflow: The expression "(unsigned int)b & mdl" is deemed underflowed because at least one of its arguments has underflowed.
unzip60/explode.c:420: assign: Assigning: "d" = "(unsigned int)b & mdl".
unzip60/explode.c:444: overflow: The expression "d &= 0xffffL" is deemed underflowed because at least one of its arguments has underflowed.
unzip60/explode.c:444: overflow: The expression "65536L - (((d &= 0xffffL) > w) ? d : w)" is deemed underflowed because at least one of its arguments has underflowed.
unzip60/explode.c:444: cast_overflow: An assign that casts to a different type, which might trigger an overflow.
unzip60/explode.c:450: overflow: The expression "w += e" is deemed underflowed because at least one of its arguments has underflowed.
unzip60/explode.c:464: deref_overflow: "w++", which might have underflowed, is passed to "G.area.Slide[w++]".
#  462|   #endif /* !NOMEMCPY */
#  463|               do {
#  464|->               redirSlide[w++] = redirSlide[d++];
#  465|               } while (--e);
#  466|           if (w == wszimpl)', true, 'Issue', 'low level bit operations', 'Tainted data from `readbyte()` is used in unsigned contexts without explicit sanitization (line 406), potentially leading to unexpected behavior, underflows (lines 414, 418, 420, 444), and dereferencing issues (line 464), with no clear guarantees of safety across all execution paths.', '2025-11-18 16:18:56.240797');
INSERT INTO public.ground_truth VALUES (562, 'unzip-6.0-63.el10', 5, 'Error: OVERRUN (CWE-119):
unzip60/inflate.c:977: cond_at_most: Checking "e < 31U" implies that "e" and "t->e" may be up to 30 on the true branch.
unzip60/inflate.c:981: overrun-local: Overrunning array "mask_bits" of 17 4-byte elements at element index 30 (byte offset 123) using index "e" (which evaluates to 30).
#  979|           /* get length of block to copy */
#  980|           NEEDBITS(e)
#  981|->         n = t->v.n + ((unsigned)b & mask_bits[e]);
#  982|           DUMPBITS(e)
#  983|', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-44660

Valid values for e is 0..16, 31,32..64 and 99 -> since the code checks whether e<31 then only values 0..16 should be valid here, this however depends on the calling functions being valid -> an extra check/assert might be useful here', 'Accessing `mask_bits[30]` at line 981 exceeds the array''s bounds (valid indices 0-16), directly correlating with CWE-119: Buffer Overflow, as `e < 31U` (line 977) does not prevent `e` from being 30.', '2025-11-18 16:18:56.240797');
INSERT INTO public.ground_truth VALUES (563, 'unzip-6.0-63.el10', 6, 'Error: OVERRUN (CWE-119):
unzip60/inflate.c:1608: cond_const: Checking "j <= 16U" implies that "j" is 17 on the false branch.
unzip60/inflate.c:1624: overrun-local: Overrunning array "c" of 17 4-byte elements at element index 17 (byte offset 71) using index "j" (which evaluates to 17).
# 1622|     /* Adjust last length count to fill out codes, if needed */
# 1623|     for (y = 1 << j; j < i; j++, y <<= 1)
# 1624|->     if ((y -= c[j]) < 0)
# 1625|         return 2;                 /* bad input: more codes than bits */
# 1626|     if ((y -= c[i]) < 0)', true, 'Non-Issue', 'i is between 1 and BMAX, line 1623 checks that j < i, array C is of the size BMAX+1', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.240797');
INSERT INTO public.ground_truth VALUES (570, 'unzip-6.0-63.el10', 13, 'Error: INTEGER_OVERFLOW (CWE-190):
unzip60/explode.c:406: tainted_data_return: The value returned by "readbyte()" is considered tainted.
unzip60/explode.c:406: overflow: The expression "(ulg)((G.incnt-- > 0) ? (int)*G.inptr++ : readbyte()) << k" might be negative, but is used in a context that treats it as unsigned.
unzip60/explode.c:406: overflow: The expression "b |= (ulg)((G.incnt-- > 0) ? (int)*G.inptr++ : readbyte()) << k" is deemed underflowed because at least one of its arguments has underflowed.
unzip60/explode.c:414: overflow: The expression "b >>= 8" is deemed underflowed because at least one of its arguments has underflowed.
unzip60/explode.c:418: overflow: The expression "b >>= 1" is deemed underflowed because at least one of its arguments has underflowed.
unzip60/explode.c:420: overflow: The expression "(unsigned int)b & mdl" is deemed underflowed because at least one of its arguments has underflowed.
unzip60/explode.c:420: assign: Assigning: "d" = "(unsigned int)b & mdl".
unzip60/explode.c:444: overflow: The expression "d &= 0xffffL" is deemed underflowed because at least one of its arguments has underflowed.
unzip60/explode.c:444: overflow: The expression "65536L - (((d &= 0xffffL) > w) ? d : w)" is deemed underflowed because at least one of its arguments has underflowed.
unzip60/explode.c:444: cast_overflow: An assign that casts to a different type, which might trigger an overflow.
unzip60/explode.c:450: overflow: The expression "w += e" is deemed underflowed because at least one of its arguments has underflowed.
unzip60/explode.c:468: overflow_sink: "(ulg)w", which might have underflowed, is passed to "flush(G.area.Slide, (ulg)w, 0)".
#  466|           if (w == wszimpl)
#  467|           {
#  468|->           if ((retval = flush(__G__ redirSlide, (ulg)w, 0)) != 0)
#  469|               return retval;
#  470|             w = u = 0;', true, 'Non-Issue', 'low level bit operations', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.240797');
INSERT INTO public.ground_truth VALUES (564, 'unzip-6.0-63.el10', 7, 'Error: COMPILER_WARNING:
unzip60/unzpriv.h:2728:53: warning[-Wformat-overflow=]: ‘%s’ directive writing up to 74535 bytes into a region of size 65528
# 2728 |        (*G.message)((zvoid *)&G, (uch *)(buf), (ulg)sprintf sprf_arg, (flag))
/usr/include/bits/stdio2.h:30:10: note: ‘__sprintf_chk’ output between 10 and 74545 bytes into a destination of size 65536
#   30 |   return __builtin___sprintf_chk (__s, __USE_FORTIFY_LEVEL - 1,
#      |          ^~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
#   31 |                                   __glibc_objsize (__s), __fmt,
#      |                                   ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
#   32 |                                   __va_arg_pack ());
#      |                                   ~~~~~~~~~~~~~~~~~
# 2726|   #    ifdef INT_SPRINTF  /* optimized version for "int sprintf()" flavour */
# 2727|   #      define Info(buf,flag,sprf_arg) \
# 2728|->        (*G.message)((zvoid *)&G, (uch *)(buf), (ulg)sprintf sprf_arg, (flag))
# 2729|   #    else          /* generic version, does not use sprintf() return value */
# 2730|   #      define Info(buf,flag,sprf_arg) \', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-44659

Plausible, needs some further checks (why exactly 74535 bytes?)', 'Direct buffer overflow risk exists at `unzip60/unzpriv.h:2728:53` due to `sprintf` potentially writing up to 74535 bytes into a 65528-byte buffer, with no explicit bounds checking in the provided `Info` macro''s `INT_SPRINTF` branch.', '2025-11-18 16:18:56.240797');
INSERT INTO public.ground_truth VALUES (565, 'unzip-6.0-63.el10', 8, 'Error: OVERRUN (CWE-119):
unzip60/inflate.c:1608: cond_const: Checking "j <= 16U" implies that "j" is 17 on the false branch.
unzip60/inflate.c:1613: assignment: Assigning: "*m" = "j". The value of "*m" is now 17.
unzip60/inflate.c:1614: assignment: Assigning: "i" = "16U".
unzip60/inflate.c:1614: decr: Decrementing "i". The value of "i" is now 15.
unzip60/inflate.c:1614: cond_at_least: Checking "i" implies that "i" is at least 1 on the true branch.
unzip60/inflate.c:1618: cond_at_least: Checking "*m > i" implies that "g" and "i" are at least 17 on the false branch.
unzip60/inflate.c:1626: overrun-local: Overrunning array "c" of 17 4-byte elements at element index 17 (byte offset 71) using index "i" (which evaluates to 17).
# 1624|       if ((y -= c[j]) < 0)
# 1625|         return 2;                 /* bad input: more codes than bits */
# 1626|->   if ((y -= c[i]) < 0)
# 1627|       return 2;
# 1628|     c[i] += y;', true, 'Non-Issue', 'C is an array of size BMAX+1, i is between 1 and BMAX (inclusive)', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.240797');
INSERT INTO public.ground_truth VALUES (566, 'unzip-6.0-63.el10', 9, 'Error: INTEGER_OVERFLOW (CWE-190):
unzip60/explode.c:406: tainted_data_return: The value returned by "readbyte()" is considered tainted.
unzip60/explode.c:406: overflow: The expression "(ulg)((G.incnt-- > 0) ? (int)*G.inptr++ : readbyte()) << k" might be negative, but is used in a context that treats it as unsigned.
unzip60/explode.c:406: overflow: The expression "b |= (ulg)((G.incnt-- > 0) ? (int)*G.inptr++ : readbyte()) << k" is deemed underflowed because at least one of its arguments has underflowed.
unzip60/explode.c:414: overflow: The expression "b >>= 8" is deemed underflowed because at least one of its arguments has underflowed.
unzip60/explode.c:418: overflow: The expression "b >>= 1" is deemed underflowed because at least one of its arguments has underflowed.
unzip60/explode.c:420: overflow: The expression "(unsigned int)b & mdl" is deemed underflowed because at least one of its arguments has underflowed.
unzip60/explode.c:420: assign: Assigning: "d" = "(unsigned int)b & mdl".
unzip60/explode.c:444: overflow: The expression "d &= 0xffffL" is deemed underflowed because at least one of its arguments has underflowed.
unzip60/explode.c:444: overflow: The expression "65536L - (((d &= 0xffffL) > w) ? d : w)" is deemed underflowed because at least one of its arguments has underflowed.
unzip60/explode.c:444: cast_overflow: An assign that casts to a different type, which might trigger an overflow.
unzip60/explode.c:450: overflow: The expression "w += e" is deemed underflowed because at least one of its arguments has underflowed.
unzip60/explode.c:407: deref_overflow: "w++", which might have underflowed, is passed to "G.area.Slide[w++]".
#  405|         s--;
#  406|         NEEDBITS(8)
#  407|->       redirSlide[w++] = (uch)b;
#  408|         if (w == wszimpl)
#  409|         {', true, 'Issue', 'low level bit operations', 'Tainted data from `readbyte()` is used without validation (line 406), combined with potential signed-to-unsigned conversion issues and underflow in bitwise/arithmetic operations (lines 406, 414, 418, 420, 444, 450), and dereferencing with possible underflow (line 407), all without explicit safety guarantees in the provided code.', '2025-11-18 16:18:56.240797');
INSERT INTO public.ground_truth VALUES (574, 'unzip-6.0-63.el10', 17, 'Error: NEGATIVE_RETURNS (CWE-394):
unzip60/inflate.c:1652: var_tested_neg: Assigning: "h" = a negative value.
unzip60/inflate.c:1741: negative_returns: Using variable "h" as an index to array "x".
# 1739|   
# 1740|         /* backup over finished tables */
# 1741|->       while ((i & ((1 << w) - 1)) != x[h])
# 1742|           w -= l[--h];            /* don''t need to update q */
# 1743|       }', true, 'Non-Issue', 'If the code reaches this point, line 1668 (containing h++) must have been reached at least once:
both w and l(h) at line 1666 are equal to zero, k is at least 1 (line 1611) thus the condition (k > w +l[h]) must be true at least once', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.240797');
INSERT INTO public.ground_truth VALUES (567, 'unzip-6.0-63.el10', 10, 'Error: INTEGER_OVERFLOW (CWE-125):
unzip60/inflate.c:997: tainted_data_return: The value returned by "readbyte()" is considered tainted.
unzip60/inflate.c:997: assign: Assigning: "c" = "(G.incnt-- > 0) ? (int)*G.inptr++ : readbyte()".
unzip60/inflate.c:997: overflow: The expression "(ulg)c << k" might be negative, but is used in a context that treats it as unsigned.
unzip60/inflate.c:997: overflow: The expression "b |= (ulg)c << k" is deemed underflowed because at least one of its arguments has underflowed.
unzip60/inflate.c:997: overflow: The expression "b |= (ulg)c << k" is deemed underflowed because at least one of its arguments has underflowed.
unzip60/inflate.c:998: overflow: The expression "(unsigned int)b & mask_bits[e]" is deemed underflowed because at least one of its arguments has underflowed.
unzip60/inflate.c:998: overflow: The expression "(unsigned int)w - t->v.n - ((unsigned int)b & mask_bits[e])" is deemed underflowed because at least one of its arguments has underflowed.
unzip60/inflate.c:998: assign: Assigning: "d" = "(unsigned int)w - t->v.n - ((unsigned int)b & mask_bits[e])".
unzip60/inflate.c:1012: overflow: The expression "d &= 0xffffU" is deemed underflowed because at least one of its arguments has underflowed.
unzip60/inflate.c:1028: deref_overflow: "d", which might have underflowed, is passed to "G.area.Slide[d++]".
# 1026|   #endif /* !NOMEMCPY */
# 1027|               do {
# 1028|->               redirSlide[w++] = redirSlide[d++];
# 1029|               } while (--e);
# 1030|             if (w == wsize)', true, 'Non-Issue', 'low level bit operations', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.240797');
INSERT INTO public.ground_truth VALUES (568, 'unzip-6.0-63.el10', 11, 'Error: RESOURCE_LEAK (CWE-772):
unzip60/unzip.c:1089: alloc_arg: "envargs" allocates memory that is stored into "argv".
unzip60/unzip.c:1302: leaked_storage: Returning without freeing "argv" leaks the storage that it points to.
# 1300|           check_for_windows("UnZip");
# 1301|   #endif
# 1302|->     return(retcode);
# 1303|   
# 1304|   } /* end main()/unzip() */', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-44661

program ends here anyway -> system will do the cleanup', 'Memory allocated for `argv` in `envargs.c` (line 95) is stored and used in `unzip.c` but lacks a guaranteed `free` call before `unzip.c:1302` return, as `cleanup_and_exit` (line 1281) doesn''t explicitly free `argv` in the provided context.', '2025-11-18 16:18:56.240797');
INSERT INTO public.ground_truth VALUES (569, 'unzip-6.0-63.el10', 12, 'Error: COPY_PASTE_ERROR (CWE-398):
unzip60/zipinfo.c:518: original: ""error:  a valid character encoding should follow the -I argument"" looks like the original copy.
unzip60/zipinfo.c:569: copy_paste_error: ""error:  a valid character encoding should follow the -I argument"" looks like a copy-paste error.
unzip60/zipinfo.c:569: remediation: Should it say ""error:  a valid character encoding should follow the -O argument"" instead?
#  567|       						/* Assume that charsets can''t start with a dash to spot arguments misuse */
#  568|       						if(*s == ''-'') { 
#  569|->     	                        Info(slide, 0x401, ((char *)slide,
#  570|           		                  "error:  a valid character encoding should follow the -I argument"));
#  571|       	                        return(PK_PARAM);', true, 'Non-Issue', 'intended', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.240797');
INSERT INTO public.ground_truth VALUES (571, 'unzip-6.0-63.el10', 14, 'Error: COPY_PASTE_ERROR (CWE-398):
unzip60/unzip.c:1559: original: ""error:  a valid character encoding should follow the -I argument"" looks like the original copy.
unzip60/unzip.c:1665: copy_paste_error: ""error:  a valid character encoding should follow the -I argument"" looks like a copy-paste error.
unzip60/unzip.c:1665: remediation: Should it say ""error:  a valid character encoding should follow the -O argument"" instead?
# 1663|       						/* Assume that charsets can''t start with a dash to spot arguments misuse */
# 1664|       						if(*s == ''-'') { 
# 1665|->     	                        Info(slide, 0x401, ((char *)slide,
# 1666|           		                  "error:  a valid character encoding should follow the -I argument"));
# 1667|       	                        return(PK_PARAM);', true, 'Non-Issue', 'intended', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.240797');
INSERT INTO public.ground_truth VALUES (572, 'unzip-6.0-63.el10', 15, 'Error: INTEGER_OVERFLOW (CWE-125):
unzip60/explode.c:406: tainted_data_return: The value returned by "readbyte()" is considered tainted.
unzip60/explode.c:406: overflow: The expression "(ulg)((G.incnt-- > 0) ? (int)*G.inptr++ : readbyte()) << k" might be negative, but is used in a context that treats it as unsigned.
unzip60/explode.c:406: overflow: The expression "b |= (ulg)((G.incnt-- > 0) ? (int)*G.inptr++ : readbyte()) << k" is deemed underflowed because at least one of its arguments has underflowed.
unzip60/explode.c:414: overflow: The expression "b >>= 8" is deemed underflowed because at least one of its arguments has underflowed.
unzip60/explode.c:418: overflow: The expression "b >>= 1" is deemed underflowed because at least one of its arguments has underflowed.
unzip60/explode.c:420: overflow: The expression "(unsigned int)b & mdl" is deemed underflowed because at least one of its arguments has underflowed.
unzip60/explode.c:420: assign: Assigning: "d" = "(unsigned int)b & mdl".
unzip60/explode.c:444: overflow: The expression "d &= 0xffffL" is deemed underflowed because at least one of its arguments has underflowed.
unzip60/explode.c:451: overflow: The expression "d += e" is deemed underflowed because at least one of its arguments has underflowed.
unzip60/explode.c:444: overflow: The expression "d &= 0xffffL" is deemed underflowed because at least one of its arguments has underflowed.
unzip60/explode.c:464: deref_overflow: "d++", which might have underflowed, is passed to "G.area.Slide[d++]".
#  462|   #endif /* !NOMEMCPY */
#  463|               do {
#  464|->               redirSlide[w++] = redirSlide[d++];
#  465|               } while (--e);
#  466|           if (w == wszimpl)', true, 'Non-Issue', 'low level bit operations', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.240797');
INSERT INTO public.ground_truth VALUES (573, 'unzip-6.0-63.el10', 16, 'Error: OVERRUN (CWE-119):
unzip60/inflate.c:989: cond_at_most: Checking "(e = t->e) < 32U" implies that "e" and "t->e" may be up to 31 on the true branch.
unzip60/inflate.c:998: overrun-local: Overrunning array "mask_bits" of 17 4-byte elements at element index 31 (byte offset 127) using index "e" (which evaluates to 31).
#  996|           }
#  997|           NEEDBITS(e)
#  998|->         d = (unsigned)w - t->v.n - ((unsigned)b & mask_bits[e]);
#  999|           DUMPBITS(e)
# 1000|', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-44660

Valid values for e is 0..16, 31,32..64 and 99 -> values 0..16 should be valid here, this however depends on the calling functions being valid -> an extra check/assert might be useful here', 'Accessing `mask_bits[e]` at line 998 with `e` potentially being 31 (as allowed by the condition at line 989) exceeds the array''s valid index range (0-16) for its 17 4-byte elements, confirming the out-of-bounds overrun-local error.', '2025-11-18 16:18:56.240797');
INSERT INTO public.ground_truth VALUES (575, 'unzip-6.0-63.el10', 18, 'Error: IDENTICAL_BRANCHES (CWE-398):
unzip60/extract.c:2554: implicit_else: The code from the above if-then branch is identical to the code after the if statement.
unzip60/extract.c:2552: identical_branches: The same code is executed when the condition "G.outcnt == 0UL" is true or false, because the code in the if-then branch and after the if statement is identical. Should the if statement be removed?
# 2550|                   error = (r == 3)? PK_MEM3 : PK_ERR;
# 2551|               }
# 2552|->             if (G.outcnt == 0L)   /* inflate''s final FLUSH sets outcnt */
# 2553|                   break;
# 2554|               break;', false, 'Issue', 'RHEL SAST Automation: https://issues.redhat.com/browse/RHEL-44661

The code exits the switch branch regarldless of the check, not sure what was the intention', 'Code redundancy identified at lines 2552-2554, where `G.outcnt == 0L` and `G.outcnt != 0L` both execute `break;`, aligning with the CVE report''s `implicit_else` and `identical_branches` issue, posing a code quality concern.', '2025-11-18 16:18:56.240797');
INSERT INTO public.ground_truth VALUES (576, 'unzip-6.0-63.el10', 19, 'Error: INTEGER_OVERFLOW (CWE-190):
unzip60/explode.c:406: tainted_data_return: The value returned by "readbyte()" is considered tainted.
unzip60/explode.c:406: overflow: The expression "(ulg)((G.incnt-- > 0) ? (int)*G.inptr++ : readbyte()) << k" might be negative, but is used in a context that treats it as unsigned.
unzip60/explode.c:406: overflow: The expression "b |= (ulg)((G.incnt-- > 0) ? (int)*G.inptr++ : readbyte()) << k" is deemed underflowed because at least one of its arguments has underflowed.
unzip60/explode.c:414: overflow: The expression "b >>= 8" is deemed underflowed because at least one of its arguments has underflowed.
unzip60/explode.c:418: overflow: The expression "b >>= 1" is deemed underflowed because at least one of its arguments has underflowed.
unzip60/explode.c:420: overflow: The expression "(unsigned int)b & mdl" is deemed underflowed because at least one of its arguments has underflowed.
unzip60/explode.c:420: assign: Assigning: "d" = "(unsigned int)b & mdl".
unzip60/explode.c:444: overflow: The expression "d &= 0xffffL" is deemed underflowed because at least one of its arguments has underflowed.
unzip60/explode.c:451: overflow: The expression "d += e" is deemed underflowed because at least one of its arguments has underflowed.
unzip60/explode.c:444: overflow: The expression "d &= 0xffffL" is deemed underflowed because at least one of its arguments has underflowed.
unzip60/explode.c:444: overflow: The expression "65536L - (((d &= 0xffffL) > w) ? d : w)" is deemed underflowed because at least one of its arguments has underflowed.
unzip60/explode.c:444: cast_overflow: An assign that casts to a different type, which might trigger an overflow.
unzip60/explode.c:449: overflow_sink: "e", which might have underflowed, is passed to "memset(G.area.Slide + w, 0, e)". [Note: The source code implementation of the function has been overridden by a builtin model.]
#  447|           if (u && w <= d)
#  448|           {
#  449|->           memzero(redirSlide + w, e);
#  450|             w += e;
#  451|             d += e;', true, 'Issue', 'low level bit operations', 'Tainted data from `readbyte()` is used without explicit validation, and subsequent operations (e.g., lines 406, 414, 420, 444) may lead to unexpected behavior due to signed-to-unsigned conversions, potential underflows, and overflows, which could result in security vulnerabilities, given the lack of clear input sanitization.', '2025-11-18 16:18:56.240797');
INSERT INTO public.ground_truth VALUES (577, 'unzip-6.0-63.el10', 20, 'Error: UNINIT (CWE-457):
unzip60/inflate.c:1582: var_decl: Declaring variable "r" without initializer.
unzip60/inflate.c:1733: uninit_use: Using uninitialized value "r". Field "r.v" is uninitialized.
# 1731|         f = 1 << (k - w);
# 1732|         for (j = i >> w; j < z; j += f)
# 1733|->         q[j] = r;
# 1734|   
# 1735|         /* backwards increment the k-bit code i */', true, 'Non-Issue', 'The struct values are initialized starting at line1716', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.240797');
INSERT INTO public.ground_truth VALUES (578, 'unzip-6.0-63.el10', 21, 'Error: INTEGER_OVERFLOW (CWE-190):
unzip60/process.c:1258: tainted_data_argument: The value "*byterecL" is considered tainted.
unzip60/process.c:1274: tainted_data_transitive: Call to function "makeint64" with tainted argument "byterecL" returns tainted data.
unzip60/process.c:1274: cast_overflow: An assign that casts to a different type, which might trigger an overflow.
unzip60/process.c:1322: underflow: The cast of "ecrec64_start_offset" to a signed type could result in a negative number.
# 1320|       G.cur_zipfile_bufstart = zftello(G.zipfd);
# 1321|   #else /* !USE_STRM_INPUT */
# 1322|->     G.cur_zipfile_bufstart = zlseek(G.zipfd, ecrec64_start_offset, SEEK_SET);
# 1323|   #endif /* ?USE_STRM_INPUT */
# 1324|', true, 'Non-Issue', 'low level bit operations', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.240797');
INSERT INTO public.ground_truth VALUES (579, 'unzip-6.0-63.el10', 22, 'Error: INTEGER_OVERFLOW (CWE-125):
unzip60/inflate.c:997: tainted_data_return: The value returned by "readbyte()" is considered tainted.
unzip60/inflate.c:997: assign: Assigning: "c" = "(G.incnt-- > 0) ? (int)*G.inptr++ : readbyte()".
unzip60/inflate.c:997: overflow: The expression "(ulg)c << k" might be negative, but is used in a context that treats it as unsigned.
unzip60/inflate.c:997: overflow: The expression "b |= (ulg)c << k" is deemed underflowed because at least one of its arguments has underflowed.
unzip60/inflate.c:998: overflow: The expression "(unsigned int)b & mask_bits[e]" is deemed underflowed because at least one of its arguments has underflowed.
unzip60/inflate.c:998: overflow: The expression "(unsigned int)w - t->v.n - ((unsigned int)b & mask_bits[e])" is deemed underflowed because at least one of its arguments has underflowed.
unzip60/inflate.c:998: assign: Assigning: "d" = "(unsigned int)w - t->v.n - ((unsigned int)b & mask_bits[e])".
unzip60/inflate.c:1012: overflow: The expression "d &= 0xffffU" is deemed underflowed because at least one of its arguments has underflowed.
unzip60/inflate.c:1028: deref_overflow: "d++", which might have underflowed, is passed to "G.area.Slide[d++]".
# 1026|   #endif /* !NOMEMCPY */
# 1027|               do {
# 1028|->               redirSlide[w++] = redirSlide[d++];
# 1029|               } while (--e);
# 1030|             if (w == wsize)', true, 'Non-Issue', 'low level bit operations', 'The error is similar to one found in the provided known issues (Details in the full Justification)', '2025-11-18 16:18:56.240797');


--
-- Data for Name: mlops_batch; Type: TABLE DATA; Schema: public; Owner: quarkus
--

INSERT INTO public.mlops_batch VALUES (6, 'v1.0.1', 'v2.3.1', 'v1.1.1', 'quay.io/sast-ai/analyzer:v1.0', 'mock_user', '2025-11-13 15:40:16', '2025-11-14 00:05:00', 'completed', 23, 23, 0);


--
-- Data for Name: mlops_job; Type: TABLE DATA; Schema: public; Owner: quarkus
--

INSERT INTO public.mlops_job VALUES (93, 6, 'libksba-1.6.5-3', 'libksba', 'libksba', '1.6.5-3', 'https://src.fedoraproject.org/rpms/libksba', 'https://example.com/fp/libksba', 'https://tekton.example.com/runs/6-libksba', 'completed', '2025-11-13 15:40:16', '2025-11-13 15:40:16', '2025-11-13 15:40:16', NULL, '2025-11-13 15:40:16', 'mock_user', NULL, NULL, NULL);
INSERT INTO public.mlops_job VALUES (94, 6, 'gzip-1.13-1', 'gzip', 'gzip', '1.13-1', 'https://src.fedoraproject.org/rpms/gzip', 'https://example.com/fp/gzip', 'https://tekton.example.com/runs/6-gzip', 'completed', '2025-11-13 15:47:06', '2025-11-13 15:47:06', '2025-11-13 15:47:06', NULL, '2025-11-13 15:47:06', 'mock_user', NULL, NULL, NULL);
INSERT INTO public.mlops_job VALUES (95, 6, 'libconfig-1.7.3-8', 'libconfig', 'libconfig', '1.7.3-8', 'https://src.fedoraproject.org/rpms/libconfig', 'https://example.com/fp/libconfig', 'https://tekton.example.com/runs/6-libconfig', 'completed', '2025-11-13 15:49:03', '2025-11-13 15:49:03', '2025-11-13 15:49:03', NULL, '2025-11-13 15:49:03', 'mock_user', NULL, NULL, NULL);
INSERT INTO public.mlops_job VALUES (96, 6, 'libpcap-1.10.4-4', 'libpcap', 'libpcap', '1.10.4-4', 'https://src.fedoraproject.org/rpms/libpcap', 'https://example.com/fp/libpcap', 'https://tekton.example.com/runs/6-libpcap', 'completed', '2025-11-13 16:55:04', '2025-11-13 16:55:04', '2025-11-13 16:55:04', NULL, '2025-11-13 16:55:04', 'mock_user', NULL, NULL, NULL);
INSERT INTO public.mlops_job VALUES (97, 6, 'adcli-0.9.2-6', 'adcli', 'adcli', '0.9.2-6', 'https://src.fedoraproject.org/rpms/adcli', 'https://example.com/fp/adcli', 'https://tekton.example.com/runs/6-adcli', 'completed', '2025-11-13 17:06:19', '2025-11-13 17:06:19', '2025-11-13 17:06:19', NULL, '2025-11-13 17:06:19', 'mock_user', NULL, NULL, NULL);
INSERT INTO public.mlops_job VALUES (98, 6, 'audit-4.0-8', 'audit', 'audit', '4.0-8', 'https://src.fedoraproject.org/rpms/audit', 'https://example.com/fp/audit', 'https://tekton.example.com/runs/6-audit', 'completed', '2025-11-13 17:08:10', '2025-11-13 17:08:10', '2025-11-13 17:08:10', NULL, '2025-11-13 17:08:10', 'mock_user', NULL, NULL, NULL);
INSERT INTO public.mlops_job VALUES (99, 6, 'libpng-1.6.40-3', 'libpng', 'libpng', '1.6.40-3', 'https://src.fedoraproject.org/rpms/libpng', 'https://example.com/fp/libpng', 'https://tekton.example.com/runs/6-libpng', 'completed', '2025-11-13 17:17:39', '2025-11-13 17:17:39', '2025-11-13 17:17:39', NULL, '2025-11-13 17:17:39', 'mock_user', NULL, NULL, NULL);
INSERT INTO public.mlops_job VALUES (100, 6, 'cpio-2.15-1', 'cpio', 'cpio', '2.15-1', 'https://src.fedoraproject.org/rpms/cpio', 'https://example.com/fp/cpio', 'https://tekton.example.com/runs/6-cpio', 'completed', '2025-11-13 17:23:42', '2025-11-13 17:23:42', '2025-11-13 17:23:42', NULL, '2025-11-13 17:23:42', 'mock_user', NULL, NULL, NULL);
INSERT INTO public.mlops_job VALUES (101, 6, 'graphite2-1.3.14-15', 'graphite2', 'graphite2', '1.3.14-15', 'https://src.fedoraproject.org/rpms/graphite2', 'https://example.com/fp/graphite2', 'https://tekton.example.com/runs/6-graphite2', 'completed', '2025-11-13 18:26:25', '2025-11-13 18:26:25', '2025-11-13 18:26:25', NULL, '2025-11-13 18:26:25', 'mock_user', NULL, NULL, NULL);
INSERT INTO public.mlops_job VALUES (102, 6, 'glibc-2.39-2', 'glibc', 'glibc', '2.39-2', 'https://src.fedoraproject.org/rpms/glibc', 'https://example.com/fp/glibc', 'https://tekton.example.com/runs/6-glibc', 'completed', '2025-11-13 18:37:20', '2025-11-13 18:37:20', '2025-11-13 18:37:20', NULL, '2025-11-13 18:37:20', 'mock_user', NULL, NULL, NULL);
INSERT INTO public.mlops_job VALUES (103, 6, 'libtalloc-2.4.2-1', 'libtalloc', 'libtalloc', '2.4.2-1', 'https://src.fedoraproject.org/rpms/libtalloc', 'https://example.com/fp/libtalloc', 'https://tekton.example.com/runs/6-libtalloc', 'completed', '2025-11-13 18:43:36', '2025-11-13 18:43:36', '2025-11-13 18:43:36', NULL, '2025-11-13 18:43:36', 'mock_user', NULL, NULL, NULL);
INSERT INTO public.mlops_job VALUES (104, 6, 'libuser-0.64-7', 'libuser', 'libuser', '0.64-7', 'https://src.fedoraproject.org/rpms/libuser', 'https://example.com/fp/libuser', 'https://tekton.example.com/runs/6-libuser', 'completed', '2025-11-13 18:47:35', '2025-11-13 18:47:35', '2025-11-13 18:47:35', NULL, '2025-11-13 18:47:35', 'mock_user', NULL, NULL, NULL);
INSERT INTO public.mlops_job VALUES (105, 6, 'mpdecimal-2.5.1-9', 'mpdecimal', 'mpdecimal', '2.5.1-9', 'https://src.fedoraproject.org/rpms/mpdecimal', 'https://example.com/fp/mpdecimal', 'https://tekton.example.com/runs/6-mpdecimal', 'completed', '2025-11-13 19:13:47', '2025-11-13 19:13:47', '2025-11-13 19:13:47', NULL, '2025-11-13 19:13:47', 'mock_user', NULL, NULL, NULL);
INSERT INTO public.mlops_job VALUES (106, 6, 'nano-7.2-6', 'nano', 'nano', '7.2-6', 'https://src.fedoraproject.org/rpms/nano', 'https://example.com/fp/nano', 'https://tekton.example.com/runs/6-nano', 'completed', '2025-11-13 19:21:25', '2025-11-13 19:21:25', '2025-11-13 19:21:25', NULL, '2025-11-13 19:21:25', 'mock_user', NULL, NULL, NULL);
INSERT INTO public.mlops_job VALUES (107, 6, 'ncurses-6.4-12.20240127', 'ncurses', 'ncurses', '6.4-12.20240127', 'https://src.fedoraproject.org/rpms/ncurses', 'https://example.com/fp/ncurses', 'https://tekton.example.com/runs/6-ncurses', 'completed', '2025-11-13 19:25:52', '2025-11-13 19:25:52', '2025-11-13 19:25:52', NULL, '2025-11-13 19:25:52', 'mock_user', NULL, NULL, NULL);
INSERT INTO public.mlops_job VALUES (108, 6, 'rpcbind-1.2.6-4.rc2', 'rpcbind', 'rpcbind', '1.2.6-4.rc2', 'https://src.fedoraproject.org/rpms/rpcbind', 'https://example.com/fp/rpcbind', 'https://tekton.example.com/runs/6-rpcbind', 'completed', '2025-11-13 19:28:40', '2025-11-13 19:28:40', '2025-11-13 19:28:40', NULL, '2025-11-13 19:28:40', 'mock_user', NULL, NULL, NULL);
INSERT INTO public.mlops_job VALUES (109, 6, 'sqlite-3.45.1-2', 'sqlite', 'sqlite', '3.45.1-2', 'https://src.fedoraproject.org/rpms/sqlite', 'https://example.com/fp/sqlite', 'https://tekton.example.com/runs/6-sqlite', 'completed', '2025-11-13 20:38:41', '2025-11-13 20:38:41', '2025-11-13 20:38:41', NULL, '2025-11-13 20:38:41', 'mock_user', NULL, NULL, NULL);
INSERT INTO public.mlops_job VALUES (110, 6, 'sysfsutils-2.1.1-7', 'sysfsutils', 'sysfsutils', '2.1.1-7', 'https://src.fedoraproject.org/rpms/sysfsutils', 'https://example.com/fp/sysfsutils', 'https://tekton.example.com/runs/6-sysfsutils', 'completed', '2025-11-13 20:44:22', '2025-11-13 20:44:22', '2025-11-13 20:44:22', NULL, '2025-11-13 20:44:22', 'mock_user', NULL, NULL, NULL);
INSERT INTO public.mlops_job VALUES (111, 6, 'tpm2-tools-5.6-2', 'tpm2-tools', 'tpm2-tools', '5.6-2', 'https://src.fedoraproject.org/rpms/tpm2-tools', 'https://example.com/fp/tpm2-tools', 'https://tekton.example.com/runs/6-tpm2-tools', 'completed', '2025-11-13 21:46:47', '2025-11-13 21:46:47', '2025-11-13 21:46:47', NULL, '2025-11-13 21:46:47', 'mock_user', NULL, NULL, NULL);
INSERT INTO public.mlops_job VALUES (112, 6, 'texinfo-7.1-2', 'texinfo', 'texinfo', '7.1-2', 'https://src.fedoraproject.org/rpms/texinfo', 'https://example.com/fp/texinfo', 'https://tekton.example.com/runs/6-texinfo', 'completed', '2025-11-13 22:28:59', '2025-11-13 22:28:59', '2025-11-13 22:28:59', NULL, '2025-11-13 22:28:59', 'mock_user', NULL, NULL, NULL);
INSERT INTO public.mlops_job VALUES (113, 6, 'unzip-6.0-63', 'unzip', 'unzip', '6.0-63', 'https://src.fedoraproject.org/rpms/unzip', 'https://example.com/fp/unzip', 'https://tekton.example.com/runs/6-unzip', 'completed', '2025-11-13 23:11:56', '2025-11-13 23:11:56', '2025-11-13 23:11:56', NULL, '2025-11-13 23:11:56', 'mock_user', NULL, NULL, NULL);
INSERT INTO public.mlops_job VALUES (114, 6, 'trace-cmd-3.2-2', 'trace-cmd', 'trace-cmd', '3.2-2', 'https://src.fedoraproject.org/rpms/trace-cmd', 'https://example.com/fp/trace-cmd', 'https://tekton.example.com/runs/6-trace-cmd', 'completed', '2025-11-13 23:13:01', '2025-11-13 23:13:01', '2025-11-13 23:13:01', NULL, '2025-11-13 23:13:01', 'mock_user', NULL, NULL, NULL);
INSERT INTO public.mlops_job VALUES (115, 6, 'util-linux-2.40-0.8.rc1', 'util-linux', 'util-linux', '2.40-0.8.rc1', 'https://src.fedoraproject.org/rpms/util-linux', 'https://example.com/fp/util-linux', 'https://tekton.example.com/runs/6-util-linux', 'completed', '2025-11-14 00:05:00', '2025-11-14 00:05:00', '2025-11-14 00:05:00', NULL, '2025-11-14 00:05:00', 'mock_user', NULL, NULL, NULL);


--
-- Data for Name: mlops_job_metrics; Type: TABLE DATA; Schema: public; Owner: quarkus
--

INSERT INTO public.mlops_job_metrics VALUES (93, 93, 'libksba', 1.0000, 1.0000, 1.0000, 1.0000, 2, 0, 1, 0, '2025-11-13 15:40:16');
INSERT INTO public.mlops_job_metrics VALUES (94, 94, 'gzip', 1.0000, 1.0000, 1.0000, 1.0000, 8, 0, 3, 0, '2025-11-13 15:47:06');
INSERT INTO public.mlops_job_metrics VALUES (95, 95, 'libconfig', 1.0000, 0.0000, 0.0000, 0.0000, 2, 0, 0, 0, '2025-11-13 15:49:03');
INSERT INTO public.mlops_job_metrics VALUES (96, 96, 'libpcap', 1.0000, 0.0000, 0.0000, 0.0000, 3, 0, 0, 0, '2025-11-13 16:55:04');
INSERT INTO public.mlops_job_metrics VALUES (97, 97, 'adcli', 0.9833, 1.0000, 0.9565, 0.9778, 37, 1, 22, 0, '2025-11-13 17:06:19');
INSERT INTO public.mlops_job_metrics VALUES (98, 98, 'audit', 0.8571, 0.0000, 0.0000, 0.0000, 12, 1, 0, 1, '2025-11-13 17:08:10');
INSERT INTO public.mlops_job_metrics VALUES (99, 99, 'libpng', 1.0000, 1.0000, 1.0000, 1.0000, 11, 0, 1, 0, '2025-11-13 17:17:39');
INSERT INTO public.mlops_job_metrics VALUES (100, 100, 'cpio', 0.8788, 0.0000, 0.0000, 0.0000, 29, 4, 0, 0, '2025-11-13 17:23:42');
INSERT INTO public.mlops_job_metrics VALUES (101, 101, 'graphite2', 1.0000, 0.0000, 0.0000, 0.0000, 4, 0, 0, 0, '2025-11-13 18:26:25');
INSERT INTO public.mlops_job_metrics VALUES (102, 102, 'glibc', 0.9444, 0.9375, 0.7500, 0.8333, 87, 5, 15, 1, '2025-11-13 18:37:20');
INSERT INTO public.mlops_job_metrics VALUES (103, 103, 'libtalloc', 1.0000, 0.0000, 0.0000, 0.0000, 29, 0, 0, 0, '2025-11-13 18:43:36');
INSERT INTO public.mlops_job_metrics VALUES (104, 104, 'libuser', 1.0000, 0.0000, 0.0000, 0.0000, 8, 0, 0, 0, '2025-11-13 18:47:35');
INSERT INTO public.mlops_job_metrics VALUES (105, 105, 'mpdecimal', 0.8000, 0.0000, 0.0000, 0.0000, 20, 1, 0, 4, '2025-11-13 19:13:47');
INSERT INTO public.mlops_job_metrics VALUES (106, 106, 'nano', 0.7500, 0.0000, 0.0000, 0.0000, 9, 2, 0, 1, '2025-11-13 19:21:25');
INSERT INTO public.mlops_job_metrics VALUES (107, 107, 'ncurses', 0.7500, 0.0000, 0.0000, 0.0000, 3, 0, 0, 1, '2025-11-13 19:25:52');
INSERT INTO public.mlops_job_metrics VALUES (108, 108, 'rpcbind', 1.0000, 0.0000, 0.0000, 0.0000, 3, 0, 0, 0, '2025-11-13 19:28:40');
INSERT INTO public.mlops_job_metrics VALUES (109, 109, 'sqlite', 0.8750, 0.6316, 0.6667, 0.6487, 79, 6, 12, 7, '2025-11-13 20:38:41');
INSERT INTO public.mlops_job_metrics VALUES (110, 110, 'sysfsutils', 1.0000, 1.0000, 1.0000, 1.0000, 1, 0, 9, 0, '2025-11-13 20:44:22');
INSERT INTO public.mlops_job_metrics VALUES (111, 111, 'tpm2-tools', 0.5000, 0.0000, 0.0000, 0.0000, 1, 1, 0, 0, '2025-11-13 21:46:47');
INSERT INTO public.mlops_job_metrics VALUES (112, 112, 'texinfo', 0.7692, 0.8696, 0.7692, 0.8163, 10, 6, 20, 3, '2025-11-13 22:28:59');
INSERT INTO public.mlops_job_metrics VALUES (113, 113, 'unzip', 0.8636, 0.6667, 1.0000, 0.8000, 13, 0, 6, 3, '2025-11-13 23:11:56');
INSERT INTO public.mlops_job_metrics VALUES (114, 114, 'trace-cmd', 0.8406, 0.9355, 0.7632, 0.8406, 29, 9, 29, 2, '2025-11-13 23:13:01');
INSERT INTO public.mlops_job_metrics VALUES (115, 115, 'util-linux', 0.8400, 1.0000, 0.5789, 0.7333, 31, 8, 11, 0, '2025-11-14 00:05:00');


--
-- Name: ground_truth_id_seq; Type: SEQUENCE SET; Schema: public; Owner: quarkus
--

SELECT pg_catalog.setval('public.ground_truth_id_seq', 629, true);


--
-- Name: mlops_batch_id_seq; Type: SEQUENCE SET; Schema: public; Owner: quarkus
--

SELECT pg_catalog.setval('public.mlops_batch_id_seq', 7, true);


--
-- Name: mlops_job_id_seq; Type: SEQUENCE SET; Schema: public; Owner: quarkus
--

SELECT pg_catalog.setval('public.mlops_job_id_seq', 124, true);


--
-- Name: mlops_job_metrics_id_seq; Type: SEQUENCE SET; Schema: public; Owner: quarkus
--

SELECT pg_catalog.setval('public.mlops_job_metrics_id_seq', 124, true);


--
-- PostgreSQL database dump complete
--

\unrestrict wzZXzC8hKCpW1iStji1gJ4aWpDK2tTbAqoeVTgbSFJlAs8AEagqWp7WSxKF9uZe

