package com.emotory.backend.domain.advice.entity;

import com.emotory.backend.domain.storyResult.entity.StoryResult;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Advice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "advice_id", nullable = false)
    private Long id;

    @Column(name = "advice_title", nullable = false)
    private String title;

    @Size(max = 300)
    @Column(name = "advice_description", length = 300, nullable = false)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "result_id", nullable = false)
    private StoryResult storyResult;
}
