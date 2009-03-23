#include <stdio.h>
int main(int argc, char** argv)
{
	FILE *f = fopen(argv[1], "r");
	unsigned char c;
	while (1) {
		c = fgetc(f);
		if (feof(f)) {
			break;
		}
		printf("(byte)0x%02x, ", c);
	}
	fclose(f);
}
