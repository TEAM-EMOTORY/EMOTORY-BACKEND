package com.emotory.backend.domain.member.service;

import com.emotory.backend.domain.member.dto.request.MemberCreateRequest;
import com.emotory.backend.domain.member.dto.response.MemberNameResponse;
import com.emotory.backend.domain.member.entity.Member;
import com.emotory.backend.domain.image.client.ImageInput;
import com.emotory.backend.global.s3.service.S3FileService;
import com.emotory.backend.global.s3.service.S3StorageService;
import com.emotory.backend.global.exception.member.MemberNotFoundException;
import com.emotory.backend.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final S3StorageService storage;
    private final S3FileService files;

    @Transactional
    public Long createMember(MemberCreateRequest request) {
        String sourceKey = files.extractFaceImageKey(request.faceImageUrl());
        ImageInput image = ImageInput.from(storage.downloadFile(sourceKey));
        String snapshotKey = "image-references/members/" + UUID.randomUUID() + "." + image.extension();
        storage.uploadFile(snapshotKey, image.bytes(), image.contentType());
        Member member = Member.create(request.name(), request.faceImageUrl(), image.sha256(), snapshotKey);
        memberRepository.save(member);
        return member.getId();
    }

    public MemberNameResponse getMemberName(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(MemberNotFoundException::new);

        return MemberNameResponse.from(member);
    }
}
