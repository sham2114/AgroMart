package com.agrishop.resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.agrishop.dao.CartDao;
import com.agrishop.dao.ProductDao;
import com.agrishop.dao.UserDao;
import com.agrishop.dto.AddToCartRequest;
import com.agrishop.dto.CartDataResponse;
import com.agrishop.dto.CartResponse;
import com.agrishop.dto.CommonApiResponse;
import com.agrishop.exception.CartSaveFailedException;
import com.agrishop.model.Cart;
import com.agrishop.model.Product;
import com.agrishop.model.User;

@Component	
public class CartResource {

	@Autowired
	private CartDao cartDao;

	@Autowired
	private UserDao userDao;

	@Autowired
	private ProductDao productDao;

	ObjectMapper objectMapper = new ObjectMapper();

	public ResponseEntity<CommonApiResponse> add(AddToCartRequest addToCartRequest) {
		CommonApiResponse response = new CommonApiResponse();

		if (addToCartRequest == null) {
			response.setResponseMessage("bad request - missing request");
			response.setSuccess(false);

			return new ResponseEntity<CommonApiResponse>(response, HttpStatus.BAD_REQUEST);
		}

		if (!AddToCartRequest.validateAddToCartRequest(addToCartRequest)) {
			response.setResponseMessage("bad request - missing field");
			response.setSuccess(false);

			return new ResponseEntity<CommonApiResponse>(response, HttpStatus.BAD_REQUEST);
		}

		Optional<User> optionalUser = userDao.findById(addToCartRequest.getUserId());
		User user = null;
		if (optionalUser.isPresent()) {
			user = optionalUser.get();
		}

		if (user == null) {
			response.setResponseMessage("bad request - user not found");
			response.setSuccess(false);

			return new ResponseEntity<CommonApiResponse>(response, HttpStatus.BAD_REQUEST);
		}

		Optional<Product> optionalProduct = productDao.findById(addToCartRequest.getProductId());
		Product product = null;
		if (optionalProduct.isPresent()) {
			product = optionalProduct.get();
		}

		if (product == null) {
			response.setResponseMessage("bad request - product not found");
			response.setSuccess(false);

			return new ResponseEntity<CommonApiResponse>(response, HttpStatus.BAD_REQUEST);
		}

		Cart cart = new Cart();
		cart.setProduct(product);
		cart.setQuantity(addToCartRequest.getQuantity());
		cart.setUser(user);

		Cart addedCart = cartDao.save(cart);

		if (addedCart == null) {
			throw new CartSaveFailedException("Failed to Save the Cart");
		}

		response.setResponseMessage("Cart added successful!!!");
		response.setSuccess(true);

		return new ResponseEntity<CommonApiResponse>(response, HttpStatus.OK);
	}

	public ResponseEntity<CartResponse> getMyCart(int userId) {
		CartResponse response = new CartResponse();

		List<CartDataResponse> cartDatas = new ArrayList<>();

		if (userId == 0) {
			response.setResponseMessage("missing input user id");
			response.setSuccess(false);

			return new ResponseEntity<CartResponse>(response, HttpStatus.BAD_REQUEST);
		}

		User user = null;

		Optional<User> optional = userDao.findById(userId);

		if (optional.isPresent()) {
			user = optional.get();
		}

		if (user == null) {
			response.setResponseMessage("user not found");
			response.setSuccess(false);

			return new ResponseEntity<CartResponse>(response, HttpStatus.BAD_REQUEST);
		}

		List<Cart> userCarts = cartDao.findByUser_id(userId);

		if (CollectionUtils.isEmpty(userCarts)) {
			response.setResponseMessage("User carts not found!!!");
			response.setSuccess(false);

			return new ResponseEntity<CartResponse>(response, HttpStatus.OK);
		}

		double totalCartPrice = 0;

		for (Cart cart : userCarts) {
			CartDataResponse cartData = new CartDataResponse();
			cartData.setCartId(cart.getId());
			cartData.setProductDescription(cart.getProduct().getDescription());
			cartData.setProductName(cart.getProduct().getTitle());
			cartData.setProductImage(cart.getProduct().getImageName());
			cartData.setQuantity(cart.getQuantity());
			cartData.setProductId(cart.getProduct().getId());

			cartDatas.add(cartData);

			double productPrice = Double.parseDouble(cart.getProduct().getPrice().toString());

			totalCartPrice = totalCartPrice + (cart.getQuantity() * productPrice);

		}

		response.setTotalCartPrice(String.valueOf(totalCartPrice));
		response.setCartData(cartDatas);
		response.setResponseMessage("User cart fetched success!!!");
		response.setSuccess(true);

		return new ResponseEntity<CartResponse>(response, HttpStatus.OK);
	}

	public ResponseEntity<CommonApiResponse> removeCartItem(int cartId) {
		CommonApiResponse response = new CommonApiResponse();

		if (cartId == 0) {
			response.setResponseMessage("bad request - missing input");
			response.setSuccess(false);

			return new ResponseEntity<CommonApiResponse>(response, HttpStatus.BAD_REQUEST);
		}

		Optional<Cart> optionalCart = this.cartDao.findById(cartId);
		Cart cart = new Cart();

		if (optionalCart.isPresent()) {
			cart = optionalCart.get();
		}

		if (cart == null) {
			response.setResponseMessage("Cart not found!!!");
			response.setSuccess(false);

			return new ResponseEntity<CommonApiResponse>(response, HttpStatus.BAD_REQUEST);
		}

		try {
			this.cartDao.delete(cart);
		} catch (Exception e) {
			response.setResponseMessage("Failed to delete Cart!!!");
			response.setSuccess(false);

			return new ResponseEntity<CommonApiResponse>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}

		response.setResponseMessage("product deleted from Cart Successfull!!!");
		response.setSuccess(true);

		return new ResponseEntity<CommonApiResponse>(response, HttpStatus.OK);
	}

}
