package com.example.eveant.priceList;

public class PriceListItem {
    private Integer id;
    private String name;
    private Long price;
    private Integer discount;

    public PriceListItem() {
    }

    public PriceListItem( String name, long price, int discount) {
        this.name = name;
        this.price = price;
        this.discount = discount;
    }

    public Integer getId(){
        return id;
    }

    public void setId(Integer id){
        this.id=id;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getPrice() {
        return price;
    }

    public void setPrice(long price) {
        this.price = price;
    }

    public int getDiscount() {
        return discount;
    }

    public void setDiscount(int discount) {
        this.discount = discount;
    }

    public long getPriceWithDiscount() {
        return price - (price * discount / 100);
    }

}
