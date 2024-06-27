const express = require('express');
const productController = require('../controllers/productController');
const { verifyToken,verifyOptionalToken } = require('../middleware/auth');

const router = express.Router();

router.get('/',verifyOptionalToken, productController.getProducts);

router.get('/:id', productController.getProductById);

router.post('/', verifyToken, productController.createProduct);

router.put('/:id', verifyToken, productController.updateProduct);

router.delete('/:id', verifyToken, productController.deleteProduct);

router.post('/wishlist', verifyToken, productController.addToWishlist);

router.post('/favorites', verifyToken, productController.addToFavorites);

module.exports = router;
