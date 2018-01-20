package networklayer;

class X{
    int a;
    private int b;
    X(int i,int j){
        a=i;
        b=j;

    }
    X(int j)
    {
        b=j;
    }
    public int getb()
    {
        return b;
    }



}

class Y extends X {
    int c;
    Y(){
        super(2);
        c=2;

    }
    Y(int i, int j,int k){
        super(j);

        c=k;

        a=i;

    }
    void show() {
        System.out.println(a + getb() + c);
    }
}