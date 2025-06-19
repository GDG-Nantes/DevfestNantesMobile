# Compose Multiplatform Migration Strategy & Process

## Introduction

This document outlines the strategy, motivations, and step-by-step process for migrating our KMP
project (with Jetpack Compose and SwiftUI UIs) to a full Compose Multiplatform project. It is
intended as a reference for the team and as material for future talks or return-of-experience (REX)
sessions.

## Motivation

- Unify UI codebase for Android and iOS using Compose Multiplatform
- Reduce code duplication and maintenance overhead
- Accelerate feature delivery and ensure UI consistency
- Leverage modern, declarative UI paradigms across platforms

## Migration Principles

- **Test-Driven Development (TDD):** Write or update tests before implementing or refactoring
  features.
- **Incremental Migration:** Migrate features and screens step by step, starting with the
  simplest/common ones.
- **Documentation:** Continuously document decisions, challenges, and solutions throughout the
  migration.

## High-Level Migration Plan

1. **Project Preparation**
    - Create a new module for shared UI using Compose Multiplatform.
    - Set up dependencies and basic configuration.
2. **Testing Foundation**
    - Establish a test framework for the new shared UI module.
    - Add initial sample tests to enforce TDD.
3. **Incremental UI Migration**
    - Migrate simple/common screens first, then more complex ones.
    - For each screen: write tests, migrate UI/logic, integrate with Android/iOS.
4. **Platform-Specific Integrations**
    - Keep platform-specific code for features like map intents, using expect/actual or interfaces.
5. **Cleanup**
    - Remove obsolete native UI code and update documentation.

## Next Steps

- [ ] Create shared Compose Multiplatform UI module
- [ ] Set up Compose Multiplatform dependencies
- [ ] Establish test framework
- [ ] Start incremental migration

---

_This document will be updated throughout the migration process to capture key decisions,
challenges, and learnings._

