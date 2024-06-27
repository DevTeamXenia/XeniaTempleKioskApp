const User = require('../models/user');
const jwt = require('jsonwebtoken');
const bcrypt = require('bcryptjs');

exports.register = async (req, res) => {
    const { phoneNumber, password } = req.body;

    try {
        let user = await User.findOne({ phoneNumber });
        if (user) {
            return res.status(400).json({ status : false, message: 'User already exists' });
        }

        user = new User({
            phoneNumber,
            password,
        });

        await user.save();

        const payload = {
            user: {
                id: user.id,
            },
        };

        jwt.sign(payload, process.env.JWT_SECRET, {
            expiresIn: '1h',
        }, (err, token) => {
            if (err) throw err;
            res.json({ token });
        });

    } catch (err) {
        console.log(err);
        res.status(500).json({ status : false, message: 'Internal server error' });
    }
};

exports.login = async (req, res) => {
    const { phoneNumber, password } = req.body;

    try {
        let user = await User.findOne({ phoneNumber });

        if (!user) {
            return res.status(400).json({status : false, message: 'User not exist!' });
        }


        const isMatch = await bcrypt.compare(password, user.password);

        if (!isMatch) {
            return res.status(400).json({status : false, message: 'Incorrect password' });
        }

        const payload = {
            user: {
                id: user.id,
            },
        };

        jwt.sign(payload, process.env.JWT_SECRET, { expiresIn: '1h' }, (err, token) => {
            if (err) throw err;
            res.json({ token });
        });

    } catch (err) {
        console.error(err.message);
        res.status(500).json({ status : false, message: 'Internal server error' });
    }
};

exports.getUser = async (req, res) => {
    try {
        const user = await User.findById(req.user.user.id).select('-password -wishlist -favorites');
        res.json(user);
    } catch (err) {
        res.status(500).json({ status : false, message: 'Internal server error' });
    }
};
