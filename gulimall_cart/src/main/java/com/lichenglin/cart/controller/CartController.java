package com.lichenglin.cart.controller;

import com.lichenglin.cart.feign.ProductFeign;
import com.lichenglin.cart.interceptor.CartInterceptor;
import com.lichenglin.cart.service.CartService;
import com.lichenglin.cart.vo.AddCommodityToCartVo;
import com.lichenglin.cart.vo.Cart;
import com.lichenglin.cart.vo.CartItem;
import com.lichenglin.cart.vo.UserInfoVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
public class CartController {

    @Autowired
    CartService cartService;
    /**
     * 浏览器保存user-key的cookie，标识用户身份，一个月有效期；
     * 如果第一次使用购物车功能，都会分配一个临时用户身份，浏览器会保存，以后每次访问都会携带；
     *
     *
     * 登录：session保存；
     * 未登录：cookie的user-key保存；
     * 没有临时用户，需要创建临时用户；
     * @return
     */
    @GetMapping("/cart.html")
    public String cartListPage(Model model){
        UserInfoVo userInfoVo = CartInterceptor.threadLocal.get();
        /*
            获取购物车；
         */
        Cart cart = cartService.getCart();
        model.addAttribute("cart",cart);
        return "cartList";
    }

    /**
     * redirectAttribute:
     *  addFlushAttribute() : 模拟session的方式，将数据放在session中，可以在页面取出，但是只能取一次；
     *  addAttribute():  将数据放在url地址后面；
     * @param addCommodityToCartVo
     * @param ra
     * @return
     */
    @GetMapping("/addToCart")
    public String addToCart(AddCommodityToCartVo addCommodityToCartVo, RedirectAttributes ra){
        CartItem cartItem = cartService.addToCart(addCommodityToCartVo);
        ra.addAttribute("skuId",addCommodityToCartVo.getSkuId());
        return "redirect:http://cart.gulimall.com/addToCartSuccess.html";
    }


    @GetMapping("/addToCartSuccess.html")
    public String addToCartSuccessPage(@RequestParam("skuId") Long skuId,Model model){
        //重定向到成功页面，再次查询购物车获取数据，不适用redirectAttribute是因为该api只有一次重定向时才有效；
        CartItem cartItem = cartService.getCartItem(skuId);
        model.addAttribute("item",cartItem);
        return "success";
    }

    @GetMapping("/checkItem")
    public String checkItem(@RequestParam("skuId") Long skuId,@RequestParam("check") Integer check){
        cartService.checkItem(skuId,check);
        return "redirect:http://cart.gulimall.com/cart.html";
    }

    @GetMapping("/changeItemCount")
    public String changeItemCount(@RequestParam("skuId") Long skuId,@RequestParam("num") Integer num){
        cartService.changeItemCount(skuId,num);
        return "redirect:http://cart.gulimall.com/cart.html";
    }

    @GetMapping("/deleteItem")
    public String deleteItem(@RequestParam("skuId") Long skuId){
        cartService.deleteItem(skuId);
        return "redirect:http://cart.gulimall.com/cart.html";
    }

    @GetMapping("/cartItems")
    @ResponseBody
    public List<CartItem> getCartItems(){
        // session作用域中有用户的id信息，因此不需要传入
        return cartService.getCartItemsByCurrentUser();
    }
}
