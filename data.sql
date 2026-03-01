CREATE TABLE IF NOT EXISTS plants (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nama VARCHAR(100) NOT NULL,
    path_gambar VARCHAR(255) NOT NULL,
    deskripsi TEXT NOT NULL,
    harga TEXT NOT NULL,
    pengaruh TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);