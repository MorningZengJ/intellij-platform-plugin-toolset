package com.github.morningzeng.toolset.model;

import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;

/**
 * @author Morning Zeng
 * @since 2024-07-15
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public final class Remind extends Children<Remind> {

    @Builder.Default
    private final Set<DayOfWeek> cycleDayOfWeeks = Sets.newHashSet();
    private String name;
    private LocalDateTime date;
    private String content;
    private boolean cycle;
    private ChronoUnit cycleUnit;
    private int intervalTime;
    private transient ScheduledFuture<?> scheduledFuture;

    @Override
    public String name() {
        return this.name;
    }

    public String content() {
        return Optional.ofNullable(this.content).orElse("");
    }

}
