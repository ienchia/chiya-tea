CC=gcc

CFLAGS=`pkg-config --cflags lcm`
LDFLAGS=`pkg-config --libs lcm` -lm -lev

all: kettle cup

kettle: kettle.o chat_tea_t.o cJSON.o jsonrpc-c.o
	$(CC) -o $@ $^ $(LDFLAGS)

kettle.o cJSON.o jsonrpc-c.o: kettle.c chat_tea_t.c chat_tea_t.h jsonrpc-c.h cJSON.h jsonrpc-c.c cJSON.c
	$(CC) $(CFLAGS) -c $^

chat_tea_t.o: chat_tea_t.c chat_tea_t.h
	$(CC) $(CFLAGS) -c $^

chat_tea_t.c chat_tea_t.h: tea_t.lcm
	lcm-gen -c $<

cup: lcm.jar Cup.java chat
	javac -cp .:lcm.jar Cup.java chat/*.java

chat: tea_t.lcm
	lcm-gen -j tea_t.lcm

clean:
	rm -f *.o *.class
	rm -rf chat_*
	rm -f kettle
