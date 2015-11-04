all: kettle cup

kettle: kettle.o bin
	g++ kettle.o -o bin/kettle

bin:	bin/
	mkdir bin

kettle.o: kettle.c chat_tea_t.c
	g++ -c kettle.c

chat_tea_t.c: tea_t.lcm
	lcm-gen -c tea_t.lcm

cup: lcm.jar Cup.java chat
	javac -cp .:lcm.jar Cup.java chat/*.java

chat: tea_t.lcm
	lcm-gen -j tea_t.lcm

clean:
	rm *.o bin/* *.class
	rm -r chat*
