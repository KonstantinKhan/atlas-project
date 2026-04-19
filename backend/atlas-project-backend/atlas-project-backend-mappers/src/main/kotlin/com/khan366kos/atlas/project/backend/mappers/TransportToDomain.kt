package com.khan366kos.atlas.project.backend.mappers

import com.khan366kos.atlas.project.backend.common.enums.DependencyType
import com.khan366kos.atlas.project.backend.common.models.ProjectDate
import com.khan366kos.atlas.project.backend.common.models.portfolio.Portfolio
import com.khan366kos.atlas.project.backend.common.models.portfolio.PortfolioDescription
import com.khan366kos.atlas.project.backend.common.models.portfolio.PortfolioId
import com.khan366kos.atlas.project.backend.common.models.portfolio.PortfolioName
import com.khan366kos.atlas.project.backend.common.models.taskSchedule.TaskSchedule
import com.khan366kos.atlas.project.backend.common.models.taskSchedule.TaskScheduleId
import com.khan366kos.atlas.project.backend.common.models.user.User
import com.khan366kos.atlas.project.backend.common.models.user.UserAge
import com.khan366kos.atlas.project.backend.common.models.user.UserId
import com.khan366kos.atlas.project.backend.common.models.user.UserName
import com.khan366kos.atlas.project.backend.common.models.user.UserRole
import com.khan366kos.atlas.project.backend.common.project.ProjectPriority
import com.khan366kos.atlas.project.backend.transport.commands.ChangeTaskEndDateCommandDto
import com.khan366kos.atlas.project.backend.transport.commands.ChangeTaskStartDateCommandDto
import com.khan366kos.atlas.project.backend.transport.enums.DependencyTypeDto
import com.khan366kos.atlas.project.backend.transport.enums.ProjectPriorityDto
import com.khan366kos.atlas.project.backend.transport.portfolio.CreatablePortfolioDto
import com.khan366kos.atlas.project.backend.transport.portfolio.UpdatablePortfolioDto
import com.khan366kos.atlas.project.backend.transport.user.CreatableUserDto
import com.khan366kos.atlas.project.backend.transport.user.UpdatableUserDto

fun ChangeTaskStartDateCommandDto.toDomain() = TaskSchedule(
    id = TaskScheduleId(taskId),
    start = ProjectDate.Set(newPlannedStart),
)

fun ChangeTaskEndDateCommandDto.toDomain() = TaskSchedule(
    id = TaskScheduleId(taskId),
    end = ProjectDate.Set(newPlannedEnd),
)

fun DependencyTypeDto.toDomain() = DependencyType.valueOf(this.name)

fun ProjectPriorityDto.toDomain() = ProjectPriority.valueOf(this.name)

fun CreatablePortfolioDto.toDomain() = Portfolio(
    id = PortfolioId.NONE,
    name = PortfolioName(name ?: ""),
    description = PortfolioDescription(description ?: ""),
)

fun UpdatablePortfolioDto.toDomain() = Portfolio(
    id = id?.let { PortfolioId(it) } ?: PortfolioId.NONE,
    name = PortfolioName(name ?: ""),
    description = PortfolioDescription(description ?: ""),
)

fun CreatableUserDto.toDomain(): User = User(
    id = UserId.NONE,
    name = UserName(this.name),
    age = UserAge(this.age),
    role = UserRole.valueOf(this.role),
)

fun User.applyUpdate(dto: UpdatableUserDto): User = this.copy(
    name = dto.name?.let { UserName(it) } ?: this.name,
    age = dto.age?.let { UserAge(it) } ?: this.age,
    role = dto.role?.let { UserRole.valueOf(it) } ?: this.role,
)