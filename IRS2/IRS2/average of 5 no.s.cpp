#include<stdio.h>
#include<conio.h>
int main()
{
    int a,b,c,d,e,f,g;
    
    
    
    printf("Enter five numbers");
    scanf("%d %d %d %d %d",&a,&b,&c,&d,&e);
    
    f=(a+b+c+d+e);
    
    printf("Sum of five numbers is %d",f);
    
    g=(f/5);
    
    printf("Average of five taken numbers is %d",g);
    
getch();
}
