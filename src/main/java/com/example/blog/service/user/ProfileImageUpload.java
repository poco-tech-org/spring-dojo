package com.example.blog.service.user;

import java.net.URI;

public record ProfileImageUpload(
        URI uploadURL,
        String imagePath
) {
}
