package com.guatequeriders.api.membership.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.guatequeriders.api.membership.dto.MemberCardResponse;
import com.guatequeriders.api.membership.dto.MembershipVerificationResponse;
import com.guatequeriders.api.membership.security.MembershipTokenService;
import com.guatequeriders.api.users.model.User;
import com.guatequeriders.api.users.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Base64;

@Service
public class MembershipService {

    private final UserRepository userRepository;
    private final MembershipTokenService tokenService;
    private final String publicUrl;

    public MembershipService(
            UserRepository userRepository,
            MembershipTokenService tokenService,
            @Value("${app.public-url:http://localhost:5173}") String publicUrl
    ) {
        this.userRepository = userRepository;
        this.tokenService = tokenService;
        this.publicUrl = publicUrl.replaceAll("/+$", "");
    }

    @Transactional(readOnly = true)
    public MemberCardResponse getCard(User user) {
        User current = userRepository.findById(user.getId())
                .orElseThrow(() -> new AccessDeniedException("Membership unavailable"));

        ensureActive(current);

        MembershipTokenService.IssuedVerification verification =
                tokenService.issue(current.getId(), current.getMembershipVersion());

        String verificationUrl = publicUrl + "/verify-member/"
                + URLEncoder.encode(verification.token(), StandardCharsets.UTF_8);

        return new MemberCardResponse(
                current.getMemberNumber(),
                current.getUsername(),
                current.getMembershipValidUntil(),
                verification.code(),
                verification.expiresAt(),
                toQrDataUrl(verificationUrl)
        );
    }

    @Transactional(readOnly = true)
    public MembershipVerificationResponse verify(String token) {
        MembershipTokenService.VerificationClaims claims = tokenService.parse(token);
        User user = userRepository.findById(claims.userId())
                .orElseThrow(this::unavailable);

        if (!isActive(user) || user.getMembershipVersion() != claims.membershipVersion()) {
            throw unavailable();
        }

        return new MembershipVerificationResponse(
                true,
                user.getMemberNumber(),
                user.getUsername(),
                user.getMembershipValidUntil(),
                claims.code(),
                claims.expiresAt()
        );
    }

    private void ensureActive(User user) {
        if (!isActive(user)) {
            throw new AccessDeniedException("Active membership required");
        }
    }

    private boolean isActive(User user) {
        return user.isMembershipActive()
                && user.getMemberNumber() != null
                && user.getMembershipValidUntil() != null
                && !user.getMembershipValidUntil().isBefore(LocalDate.now());
    }

    private String toQrDataUrl(String value) {
        try {
            BitMatrix matrix = new QRCodeWriter().encode(value, BarcodeFormat.QR_CODE, 360, 360);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", output);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(output.toByteArray());
        } catch (WriterException | IOException e) {
            throw new IllegalStateException("Could not generate membership QR", e);
        }
    }

    private ResponseStatusException unavailable() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Verification unavailable");
    }
}
