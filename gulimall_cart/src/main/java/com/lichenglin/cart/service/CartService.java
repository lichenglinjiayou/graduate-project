package com.lichenglin.cart.service;

import com.lichenglin.cart.vo.AddCommodityToCartVo;
import com.lichenglin.cart.vo.Cart;
import com.lichenglin.cart.vo.CartItem;

import java.util.List;

public interface CartService {
    CartItem addToCart(AddCommodityToCartVo addCommodityToCartVo);

    CartItem getCartItem(Long skuId);

    Cart getCart();

    void clearCart(String userKey);

    void checkItem(Long skuId, Integer check);

    void changeItemCount(Long skuId, Integer num);

    void deleteItem(Long skuId);

    List<CartItem> getCartItemsByCurrentUser();
}
