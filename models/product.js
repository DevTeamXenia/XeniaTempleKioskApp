
const mongoose = require('mongoose');

const ProductSchema = new mongoose.Schema({
    name: {
        type: String,
        required: true,
        unique: true,
    },
    description: {
        type: String,
    },
    salePrice: {
        type: Number,
        required: true,
    },
    offerPrice: {
        type: Number,
        required: true,
    },
    category: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'Category',
    },
    images: [{
        type: String,
    }],
    isNewArrivals: {
        type: Number,
        required: true,
    },
 
});

const Product = mongoose.model('Product', ProductSchema);

module.exports = Product;
