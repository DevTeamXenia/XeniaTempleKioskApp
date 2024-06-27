
const Category = require('../models/category');
const cloudinary = require('../config/cloudinary');
const multer = require('multer');
const { CloudinaryStorage } = require('multer-storage-cloudinary');


const storage = new CloudinaryStorage({
    cloudinary: cloudinary,
    params: {
        folder: 'categories',
        format: async (req, file) => 'jpg', 
        public_id: (req, file) => Date.now() + '-' + file.originalname.split('.')[0],
    },
});

const upload = multer({ storage });


exports.getCategories = async (req, res) => {
    try {
        const categories = await Category.find();
        res.json(categories);
    } catch (err) {
        res.status(500).json({ status : false, message: 'Internal server error' });
    }
};


exports.getCategoryById = async (req, res) => {
    try {
        const category = await Category.findById(req.params.id);
        if (!category) return res.status(404).json({ status : true, message: 'Category not found' })
        res.json(category);
    } catch (err) {
        res.status(500).json({ status : false, message: 'Internal server error' });
    }
};


exports.createCategory = [
    upload.single('image'),
    async (req, res) => {
        const { name, description } = req.body;
        const image = req.file ? req.file.path : null;

        try {
            const existingCategory = await Category.findOne({ name });
            if (existingCategory) {
                return res.status(400).json({ status : false, message: 'Category name already exists' });
            }

            const newCategory = new Category({ name, description, image });
            await newCategory.save();
            res.json(newCategory);
        } catch (err) {
            console.error(err.message);
            res.status(500).json({ status : false, message: 'Internal server error' });
        }
    },
];


exports.updateCategory = [
    upload.single('image'),
    async (req, res) => {
        const { name, description } = req.body;
        const image = req.file ? req.file.path : null;

        try {
            let category = await Category.findById(req.params.id);
            if (!category) return res.status(404).json({ status : false, message: 'Category not found' })

            if (name !== category.name) {
                const existingCategory = await Category.findOne({ name });
                if (existingCategory) {
                    return res.status(400).json({ status : false, message: 'Category name already exists' });
                }
            }

            category.name = name;
            category.description = description;
            if (image) category.image = image;

            await category.save();
            res.json(category);
        } catch (err) {
            console.error(err.message);
            res.status(500).json({ status : false, message: 'Internal server error' });
        }
    },
];


exports.deleteCategory = async (req, res) => {
    try {
        const category = await Category.findById(req.params.id);
        if (!category) return res.status(404).send({ status : false, message:'Category not found'});

        await category.remove();
        res.status(200),send({ status : true, message:'Category removed'});
    } catch (err) {
        console.error(err.message);
        res.status(500).json({ status : false, message: 'Internal server error' });
    }
};
