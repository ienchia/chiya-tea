#include <stdio.h>
#include <stddef.h>
#include <stdlib.h>
#include <unistd.h>
#include <errno.h>
#include <string.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h>
#include <arpa/inet.h>
#include <sys/wait.h>
#include <signal.h>
#include <math.h>
#include <ev.h>
#include <float.h>
#include <limits.h>
#include <ctype.h>

#include <lcm/lcm.h>
#include "chat_tea_t.h"
#include "jsonrpc-c.h"
#include "cJSON.h"

int64_t timestamp = 0;
lcm_t* lcm;
void send_message(char* sender, char* message);

#define PORT 1234  // the port users will be connecting to

struct jrpc_server my_server;

cJSON * echoback(jrpc_context * ctx, cJSON * params, cJSON *id) {
	char sender[1000] = "";
	char message[1000] = "";

	strcpy(sender,cJSON_GetObjectItem(params,"sender")->valuestring);
	strcpy(message,cJSON_GetObjectItem(params,"message")->valuestring);

	char result[1000] = "";

	sprintf(result,"%s : %s",sender,message);

	send_message(sender, message);
	printf(">>> from: %s, broadcast: %s\n", sender, message);

	return cJSON_CreateString(result);
}

void send_message(char* sender, char* message) {

	chat_tea_t tea = {
		.timestamp = timestamp++,
		.sender = sender,
		.message = message,
	};

	chat_tea_t_publish(lcm, "BROADCAST", &tea);
}

int main(int argc, char ** argv) {

	lcm = lcm_create(NULL);

	if (!lcm) {
		return -1;
	}

	jrpc_server_init(&my_server, PORT);
	jrpc_register_procedure(&my_server, echoback, "echoback", NULL );
	jrpc_server_run(&my_server);
	jrpc_server_destroy(&my_server);

	char sender[50];
	char message[50];
	while (1) {

		scanf("%s", sender);
		if (strcmp(sender, "exit\0") == 0)
			break;

		scanf("%[^\n]s\n", message);
		send_message(sender, message);
	}

	lcm_destroy(lcm);
	return 0;
}
