#include<string>
#include<iostream>
#include<stdio.h>
#include<conio.h>
#include<cstring>
using namespace std;
char matches[1075][100];
char ans[1071][100];
string matches1[1075][5];
string ans1[1075][5];
int anstotal[1071];
int matchestotal[1071];
void conv(int i,char arr[],int mode)
{
     int j,k,l;
     k=0;
     for (j =0 ; arr[j]!='\0';j++)
     {
         if (arr[j] == ',')
            break;
         //printf("%c",arr[j]);
     }
     
     int chk =1;
     
     while(arr[j]!='\0')
     {
              
              j++;
              while (arr[j]!=',' && arr[j]!='\0')
              {
                    //printf ("%c",arr[j]);
                    //getch();
                    if (mode == 1 )
                       matches1[i][k].push_back(arr[j]);
                    else
                        ans1[i][k].push_back(arr[j]);
                    j++;
              }
              //ans1[i][k].push_back('\0');
              //cout<<match
              
              //cout<<endl<<matches1[i][k]<<endl;
              //getch();
              
              k++;
     
     }
     if (mode == 1)
        matchestotal[i] = k;
     else
         anstotal[i] = k;
     //cout << k << endl;
     //getch();
}          
           
int main ()
{
    FILE *input1 = fopen("matches.txt", "r+");
    FILE *input2 = fopen("ANS1.txt", "r+");
    
    int i,j ,k;
    
    int total =0;
    for (i = 0; i<1071 ; i++)
    {
        fscanf(input1,"%s",&matches[i]);
        //fflush(stdin);
        //if (matches[strlen] ==
        //puts(matches[i]);
        conv(i,matches[i],1);
        total ++;
    }
    cout <<total <<endl;
    getch();
     total = 0;
    for ( i=0;i<1071;i++)
    {
        fscanf (input2,"%s",&ans[i]);
        //fflush(stdin);
        conv(i,ans[i],2);
        total ++;
        
    }
    cout <<total <<endl;
    getch();
    float ans = 0 ;
    int chk  =0; 
    for (i = 0; i< 1071; i++)
    {
        chk = 0;
        float max =  1;
        for (j = 0 ; j < matchestotal[i] ; j ++)
        {
            for ( k = 0 ; k < anstotal[i] ; k ++ )
            {
                if ( matches1[i][j].compare(ans1[i][k]) == 0 )
                {
                     ans += 1;
                     chk = 1;
                     //break;
                }
            }
            //if (chk == 1)
             //  break;
        }
    }
    total = 0;
    for ( i =0; i< 1071;i++)
    {
        total+=matchestotal[i];
    }
    cout << ans << "/" << total << " = " << ans/((float)total) ;getch();                                                   
}
