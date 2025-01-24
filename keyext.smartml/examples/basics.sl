contract Exchange uses CoinInfo {

    constructor(){}

    chargeBalance(){
        assert(res == CoinInfo && amount>0);
        assert(CoinInfo$balance>=amount);
        Coin$(amount - 1).transfer(sender);
    }

}