# Multi-stage build for the KB Wallet server (Ktor + Postgres/Exposed).
#
# Built from the repo root (not server/) as build context, since :server is
# one module in a multi-module Gradle build and needs settings.gradle.kts +
# gradle/ + the root build.gradle.kts alongside it — see docker-compose.yml's
# `context: .` / `dockerfile: server/Dockerfile`, and the CI job that builds
# this the same way. This also means Gradle still configures the composeApp
# module even though only :server is built; slower than a single-module repo
# would be, but correct — splitting server into its own settings.gradle.kts
# would be a bigger restructure, not something to do for build speed alone.

# ---- Build stage ----
# syntax=docker/dockerfile:1
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app
COPY . .
# This repo's root build.gradle.kts declares the Android Gradle Plugin (`apply
# false`, for composeApp's benefit) at the ROOT project level — Gradle
# resolves that classpath (100+ MB from dl.google.com) unconditionally while
# configuring the root project, even for a `:server`-only build with
# --configure-on-demand (which only skips configuring *subprojects* the task
# graph doesn't need; the root project itself is always configured). Rather
# than fight that with a bigger repo restructure, cache Gradle's dependency
# store across builds with a BuildKit mount — first build still pays for the
# download, every later one (including CI, if the gha cache backend is used
# for this stage) reuses it instead of re-fetching AGP from a cold cache,
# which is what turned a slow-but-fine download into an outright failed
# build the one time dl.google.com was slow to respond.
RUN --mount=type=cache,target=/root/.gradle \
    chmod +x gradlew && ./gradlew :server:installDist --no-daemon --console=plain --configure-on-demand

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre-jammy
RUN useradd --system --create-home --shell /usr/sbin/nologin kbwallet
WORKDIR /app
COPY --from=build /app/server/build/install/server /app
USER kbwallet
EXPOSE 8080
ENTRYPOINT ["/app/bin/server"]
