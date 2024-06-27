const jwt = require('jsonwebtoken');

const verifyToken = (req, res, next) => {
    const token = req.header('Authorization');
    if (!token) return res.status(401).json({status:false, error: 'Access denied.Token is empty' });

    try {
        const verified = jwt.verify(token, process.env.JWT_SECRET);
        req.user = verified;
        next();
    } catch (error) {
        res.status(400).json({status:false, error: 'Invalid token' });
    }
};

const verifyOptionalToken = (req, res, next) => {
    const token = req.header('Authorization');
    if (!token) {
        req.user = null; 
        return next(); 
    }

    try {
        const verified = jwt.verify(token, process.env.JWT_SECRET);
        req.user = verified;
        next(); 
    } catch (error) {
        res.status(400).json({ status: false, error: 'Invalid token' });
    }
};

module.exports = { verifyToken,verifyOptionalToken };
