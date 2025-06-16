#include <stdio.h>
#include <stdlib.h>
#include <string.h>

/* Just look at this bad code */

char* trim(const char* text){
    
    const char whitespace = ' ';
    int count = 0;
    for(int i = 0; i < strlen(text); i++){
        char c = text[i];
        if(c != whitespace) count++;
    }

    /* n is the length of the array */
    char *list = (char *) malloc(sizeof (char) * (count)); 

    int j = 0;
    for(int i = 0; i < strlen(text); i++) {
        char c = text[i];
        if(c != whitespace) list[j++] = text[i];
    }
    list[j] = '\0';
    
    return list;
}



struct Element{
    int value;
    char type;
};

struct Element* construct_elements_array(char * list){
    for(int i =0; strlen(list); i++){
        const char c = list[i];
        printf(&c);
    }
    struct Element* items = (struct Element*) malloc(sizeof(struct Element) * strlen(list));
    return items;
}


int main(){
    
    struct Element expression_as_array[2];
    const char expression[] = "1+2+5.7-9+8* 9 /8";
    char* list = trim(expression);
    construct_elements_array(list);
    if(list) free(list);
    printf(list);
    return 0;
}