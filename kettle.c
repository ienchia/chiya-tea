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
void send_message(char* floor_number, char* message);

#define PORT 1234  // the port users will be connecting to

struct jrpc_server my_server;


cJSON * evaluate_temperature(jrpc_context * ctx, cJSON * params, cJSON *id) {
	char floor_number[1000] = "";
	char temperature[1000] = "";
	char result[1000] = "";

	strcpy(floor_number,cJSON_GetObjectItem(params,"floor_number")->valuestring);
	strcpy(temperature,cJSON_GetObjectItem(params,"temperature")->valuestring);

	float temp;

	sscanf(temperature,"%f",&temp);

	if(temp < 70.0f){
		strcpy(result,"false");
	}
	else{
		printf("Detected fire on floor %s.\n", floor_number);
		printf("Turning on all sprinkler on floor %s.\n", floor_number);
		strcpy(result,"true");
		send_message(floor_number, "TURN_ON");
	}

	printf("Received temperature %s from floor %s\n",temperature,floor_number);

	return cJSON_CreateString(result);
}

void send_message(char* floor_number, char* message) {

	chat_tea_t tea = {
		.timestamp = timestamp++,
		.floor_number = floor_number,
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
	jrpc_register_procedure(&my_server, evaluate_temperature, "evaluate_temperature", NULL );
	jrpc_server_run(&my_server);
	jrpc_server_destroy(&my_server);

	lcm_destroy(lcm);
	return 0;
}
