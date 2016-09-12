//12864(ST7920)����C51����

//12864F��7920���Ĵ���ģʽC51����
#include <reg51.h>
#include <intrins.h>
sbit E_CLK =P3^0;//clock input                    ͬ��ʱ�������
sbit RW_SID=P3^1;//data input/output            �����������롢�����
//sbit RS_CS =P3^5;//chip select                    Ƭѡ��
//sbit PSB   =P3^6;//serial mode select            ����ģʽ
sbit RST   =P1^2;
void delay(unsigned int n)
{
  unsigned int i;
  for(i=0; i<n; i++) {;}
}
 //���з���һ�ֽ�����
void SendByte(unsigned char dat)
{
     unsigned char i;
     for(i=0;i<8;i++)
           {
                 E_CLK=0;
                 if(dat&0x80)RW_SID=1;else RW_SID=0;
                 E_CLK=1;
                 dat=dat<<1;
            }
}
//���н���һ�ֽ�����
unsigned char ReceieveByte(void)
{
     unsigned char i,d1,d2;
     for(i=0;i<8;i++)
           {
                 E_CLK=0;delay(100);
                 E_CLK=1;
                 if(RW_SID)d1++;
                 d1=d1<<1;
            }
     for(i=0;i<8;i++)
           {
                 E_CLK=0;delay(100);
                 E_CLK=1;
                 if(RW_SID)d2++;
                 d2=d2<<1;
            }
     return (d1&0xF0+d2&0x0F);
}
//��ȡ��־λBF
bit ReadBF(bit bf)
{
     unsigned char dat;
     SendByte(0xFA);//11111,01,0 RW=1,RS=0
     dat=ReceieveByte();
     if(dat>0x7F)bf=1;else bf=0;
     return bf;
     }
//д��������
void SendCMD(unsigned char dat)
{
//      while(ReadBF){;}
//      RS_CS=1;
     SendByte(0xF8);//11111,00,0 RW=0,RS=0 ͬ����־
     SendByte(dat&0xF0);//����λ
     SendByte((dat&0x0F)<<4);//����λ
//      RS_CS=0;
}
//д��ʾ���ݻ��ֽ��ַ�
void SendDat(unsigned char dat)
{
//      while(ReadBF){;}
//      RS_CS=1;
     SendByte(0xFA);//11111,01,0 RW=0,RS=1
     SendByte(dat&0xF0);//����λ
     SendByte((dat&0x0F)<<4);//����λ
//      RS_CS=0;
}
/*      д���ֵ�LCD ָ����λ��
     x_add��ʾRAM�ĵ�ַ
     dat1/dat2��ʾ���ֱ���
*/
void display(unsigned char x_add,unsigned char dat1,unsigned char dat2)
{
     SendCMD(x_add);//1xxx,xxxx �趨DDRAM 7λ��ַxxx,xxxx����ַ������AC
     SendDat(dat1);
     SendDat(dat2);
}
//��ʼ�� LCM
void initlcm(void)
{
     RST=0;
//      RS_CS=0;
//      PSB=0;//serial mode
     delay(100);
     RST=1;
     SendCMD(0x30);//�������ã�һ����8λ���ݣ�����ָ�
    SendCMD(0x0C);//0000,1100  ������ʾ���α�off���α�λ��off
     SendCMD(0x01);//0000,0001 ��DDRAM
     SendCMD(0x02);//0000,0010 DDRAM��ַ��λ
     SendCMD(0x80);//1000,0000 �趨DDRAM 7λ��ַ000��0000����ַ������AC
//      SendCMD(0x04);//���趨����ʾ�ַ�/����������λ��DDRAM��ַ�� һ
//      SendCMD(0x0F);//��ʾ�趨������ʾ����ʾ��꣬��ǰ��ʾλ��������
}

void main(void)
{
     initlcm();
     SendCMD(0x81);//1000,0001 �趨DDRAM 7λ��ַ000��0001����ַ������AC
     SendDat(0x33);
     SendDat(0x42);
     SendDat(0x43);
     SendDat(0x44);
     SendCMD(0x00);
     for(;;)
     {
           delay(100);
           display(0x80,0xb0,0xb2);
           display(0x81,0xbb,0xD5);
           display(0x82,0xb5,0xe7);
           display(0x83,0xc1,0xA6);
           display(0x84,0xc5,0xe0);
           display(0x85,0xD1,0xb5);
           display(0x86,0xd6,0xD0);
           display(0x87,0xd0,0xc4);
           delay(65000);
           SendCMD(0x00);
           delay(100);
           display(0x90,0xb0,0xb2);
           display(0x91,0xbb,0xD5);
           display(0x92,0xb5,0xe7);
           display(0x93,0xc1,0xA6);
           display(0x94,0xc5,0xe0);
           display(0x95,0xD1,0xb5);
           display(0x96,0xd6,0xD0);
           display(0x97,0xd0,0xc4);
           delay(65000);
           SendCMD(0x00);
           delay(100);
           display(0x88,0xb0,0xb2);
           display(0x89,0xbb,0xD5);
           display(0x8A,0xb5,0xe7);
           display(0x8B,0xc1,0xA6);
           display(0x8C,0xc5,0xe0);
           display(0x8D,0xD1,0xb5);
           display(0x8E,0xd6,0xD0);
           display(0x8F,0xd0,0xc4);
           delay(65000);
           SendCMD(0x00);
           delay(100);
           display(0x98,0xb0,0xb2);
           display(0x99,0xbb,0xD5);
           display(0x9A,0xb5,0xe7);
           display(0x9B,0xc1,0xA6);
           display(0x9C,0xc5,0xe0);
           display(0x9D,0xD1,0xb5);
           display(0x9E,0xd6,0xD0);
           display(0x9F,0xd0,0xc4);
           delay(65000);
           SendCMD(0x00);
     }
}
















