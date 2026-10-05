-- Users Table
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    token VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- Media Table
CREATE TABLE IF NOT EXISTS media (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    media_type VARCHAR(20) NOT NULL CHECK (media_type IN ('movie', 'series', 'game')),
    release_year INTEGER,
    genres TEXT[], -- Array von Genres
    age_restriction INTEGER,
    user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- Ratings Table
CREATE TABLE IF NOT EXISTS ratings (
    id SERIAL PRIMARY KEY,
    media_id INTEGER NOT NULL REFERENCES media(id) ON DELETE CASCADE,
    username VARCHAR(50) NOT NULL,
    rating INTEGER CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    is_approved BOOLEAN DEFAULT FALSE,
    likes INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(media_id, username)
    );

-- Favorites Table
CREATE TABLE IF NOT EXISTS favorites (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    media_id INTEGER NOT NULL REFERENCES media(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, media_id)
    );

-- Rating Likes Table
CREATE TABLE IF NOT EXISTS rating_likes (
    id SERIAL PRIMARY KEY,
    rating_id INTEGER NOT NULL REFERENCES ratings(id) ON DELETE CASCADE,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(rating_id, user_id)
    );

-- Leaderboard View
CREATE OR REPLACE VIEW user_leaderboard AS
SELECT u.id,
       u.username,
       COUNT(r.id) AS rating_count,
       COALESCE(AVG(r.rating), 0) AS avg_rating,
       COUNT(DISTINCT f.id) AS favorite_count
FROM users u
LEFT JOIN ratings r ON u.username = r.username
LEFT JOIN favorites f ON u.id = f.user_id
GROUP BY u.id, u.username
ORDER BY rating_count DESC, favorite_count DESC;
