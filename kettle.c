#include <lcm/lcm.h>
#include "chat_tea_t.h"

int64_t timestamp = 0;
void send_message(lcm_t* lcm, char* sender, char* message);

int main(int argc, char ** argv) {

	lcm_t* lcm = lcm_create(NULL);
		
	if (!lcm) {
		return -1;
	}
	
	send_message(lcm, "server", "Hello, World!");
	
	lcm_destroy(lcm);
	return 0;
}

void send_message(lcm_t* lcm, char* sender, char* message) {
	
	chat_tea_t tea = {
		.timestamp = timestamp++,
		.sender = sender,
		.message = message,
	};

	chat_tea_t_publish(lcm, "BROADCAST", &tea);
}
