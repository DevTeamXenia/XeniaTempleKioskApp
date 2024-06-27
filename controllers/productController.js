
const Product = require('../models/product');
const User = require('../models/user');
const cloudinary = require('../config/cloudinary');
const multer = require('multer');
const { CloudinaryStorage } = require('multer-storage-cloudinary');


const storage = new CloudinaryStorage({
    cloudinary: cloudinary,
    params: {
        folder: 'products',
        format: async (req, file) => 'jpg', 
        public_id: (req, file) => Date.now() + '-' + file.originalname.split('.')[0],
    },
});

const upload = multer({ storage }).array('images', 5);


exports.getProducts = async (req, res) => {
    try {
        let productsQuery = Product.find();
        if (req.query.category) {
            productsQuery = productsQuery.populate({
                path: 'category',
                match: { name: req.query.category }
            });
        } else {
            productsQuery = productsQuery.populate('category');
        }

        const products = await productsQuery.exec();

        if (req.user) {
            const user = await User.findById(req.user.user.id);

            const productsWithStatus = products.map(product => ({
                ...product._doc,
                wishlist: user.wishlist.includes(product._id),
                favorite: user.favorites.includes(product._id),
            }));

            res.json(productsWithStatus);
        } else {
            const productsWithStatus = products.map(product => ({
                ...product._doc,
                wishlist: false,
                favorite: false,
            }));

            res.json(productsWithStatus);
        }
    } catch (err) {
        console.error(err);
        res.status(500).json({ status: false, message: 'Internal server error' });
    }
};


exports.getProductById = async (req, res) => {
    try {
        const product = await Product.findById(req.params.id).populate('category');
        if (!product) return res.status(404).json({status : false, message: 'Product not found' });
        res.json(product);
    } catch (err) {
        res.status(500).json({ status : false, message: 'Internal server error' });
    }
};


exports.createProduct = [
    upload,
    async (req, res) => {
        const { name, description, salePrice, offerPrice, category, isNewArrivals } = req.body;
        const images = req.files ? req.files.map(file => file.path) : [];

        try {
            const newProduct = new Product({ name, description, salePrice, offerPrice, category, isNewArrivals, images });
            await newProduct.save();
            res.json(newProduct);
        } catch (err) {
            if (err.code === 11000) { 
                return res.status(400).json({status : false, message: 'Product name already exists' });
            }
            res.status(500).json({ status : false, message: 'Internal server error' });
        }
    },
];


exports.updateProduct = [
    upload,
    async (req, res) => {
        const { name, description, salePrice, offerPrice, category, isNewArrivals } = req.body;
        const images = req.files ? req.files.map(file => file.path) : null;

        try {
            let product = await Product.findById(req.params.id);
            if (!product) return res.status(404).json({status : false, message: 'Product not found' });

            product.name = name || product.name;
            product.description = description || product.description;
            product.salePrice = salePrice || product.salePrice;
            product.offerPrice = offerPrice || product.offerPrice;
            product.category = category || product.category;
            product.isNewArrivals = isNewArrivals || product.isNewArrivals;
            if (images) product.images = images;
            await product.save();
            res.json(product);
        } catch (err) {
            if (err.code === 11000) { 
                return res.status(400).json({status : false, message: 'Product name already exists' });
            }
            res.status(500).json({ status : false, message: 'Internal server error' });
        }
    },
];


exports.deleteProduct = async (req, res) => {
    try {
        const product = await Product.findById(req.params.id);
        if (!product) return res.status(404).json({status : false, message: 'Product not found' });

        await product.remove();
        res.status(200).json( {status:true,message:'Product removed'});
    } catch (err) {
        res.status(500).json({ status : false, message: 'Internal server error' });
    }
};

exports.addToWishlist = async (req, res) => {
    const { productId } = req.body;
    try {
        const user = await User.findById(req.user.user.id);
        const index = user.wishlist.indexOf(productId);
        if (index === -1) {
            user.wishlist.push(productId);
        } else {
            user.wishlist.splice(index, 1);
        }

        await user.save();
        res.status(200).json({ status: true, message: `Product ${productId} updated in wishlist successfully`, wishlist: user.wishlist });
    } catch (error) {
        console.error(error);
        res.status(500).json({ status: false, message: 'Internal server error' });
    }
};


exports.addToFavorites = async (req, res) => {
    const { productId } = req.body;
    try {
        const user = await User.findById(req.user.user.id);
        const index = user.favorites.indexOf(productId);
        if (index === -1) {
            user.favorites.push(productId);
        } else {
            user.favorites.splice(index, 1);
        }

        await user.save();
        res.status(200).json({ status: true, message: `Product ${productId} updated in favorites successfully`, favorites: user.favorites });
    } catch (error) {
        console.error(error);
        res.status(500).json({ status: false, message: 'Internal server error' });
    }
};

