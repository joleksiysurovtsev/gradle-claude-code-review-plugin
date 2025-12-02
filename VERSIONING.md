# Версионирование плагина

## Стратегия версионирования

Проект использует [Semantic Versioning](https://semver.org/) и плагин `axion-release` для автоматического управления версиями.

## Формат версии

```
MAJOR.MINOR.PATCH[-SNAPSHOT]
```

- **MAJOR**: Изменения, несовместимые с предыдущими версиями
- **MINOR**: Новый функционал, обратная совместимость сохранена
- **PATCH**: Исправления багов, обратная совместимость сохранена
- **SNAPSHOT**: Версия в разработке (не для публикации)

## Автоматическое определение версии

Версия определяется автоматически на основе git тегов:

- **На теге** (v1.2.0) → версия `1.2.0`
- **После тега** → версия `1.2.1-SNAPSHOT`
- **На ветке release** → версия без SNAPSHOT

## Команды для управления версиями

### Просмотр текущей версии
```bash
./gradlew showVersion
# или
./gradlew currentVersion
```

### Создание нового релиза

#### Автоматический способ (через GitHub Actions)
```bash
# 1. Создайте тег с новой версией
git tag v1.3.0 -m "Release v1.3.0"

# 2. Запушьте тег
git push origin v1.3.0

# 3. Запушьте в release ветку для публикации
git checkout release
git merge v1.3.0
git push origin release
```

#### Использование axion-release плагина
```bash
# Patch релиз (1.2.0 → 1.2.1)
./gradlew release -Prelease.versionIncrementer=incrementPatch

# Minor релиз (1.2.0 → 1.3.0)
./gradlew release -Prelease.versionIncrementer=incrementMinor

# Major релиз (1.2.0 → 2.0.0)
./gradlew release -Prelease.versionIncrementer=incrementMajor
```

#### Использование кастомных тасков
```bash
# Patch релиз
./gradlew nextPatchVersion

# Minor релиз
./gradlew nextMinorVersion

# Major релиз
./gradlew nextMajorVersion
```

## Версионирование по веткам

| Ветка | Версия | Пример |
|-------|--------|--------|
| main/develop | SNAPSHOT | 1.2.1-SNAPSHOT |
| release | Релизная | 1.2.1 |
| v1.2.0 (тег) | Фиксированная | 1.2.0 |

## Workflow публикации

### 1. Разработка (develop/main)
```bash
# Версия: 1.2.1-SNAPSHOT
./gradlew build
./gradlew test
```

### 2. Подготовка релиза
```bash
# Создание тега новой версии
git tag v1.3.0 -m "Release v1.3.0"
git push origin v1.3.0
```

### 3. Публикация через GitHub Actions
```bash
# Переход на release ветку запускает публикацию
git checkout release
git merge v1.3.0
git push origin release
```

### 4. Автоматическая публикация
GitHub Actions автоматически:
- Определит версию из тега (1.3.0)
- Запустит тесты
- Опубликует в Gradle Plugin Portal
- Создаст GitHub Release

## Примеры версий

```bash
# На теге v1.2.0
./gradlew currentVersion
# Output: 1.2.0

# После тега v1.2.0 (1 коммит)
./gradlew currentVersion
# Output: 1.2.1-SNAPSHOT

# На ветке release с тегом v1.2.0
./gradlew currentVersion
# Output: 1.2.0

# На ветке release без тега
./gradlew currentVersion
# Output: 1.2.1
```

## Конфигурация в build.gradle.kts

```kotlin
scmVersion {
    tag {
        prefix.set("v")           // Префикс для тегов
        versionSeparator.set("")  // Разделитель после префикса
    }

    // Стратегия инкремента по умолчанию
    versionIncrementer("incrementPatch")

    // SNAPSHOT для веток разработки
    snapshotCreator { version, _ ->
        if (version.contains("-")) {
            version
        } else {
            "$version-SNAPSHOT"
        }
    }

    // Отключение проверок для CI/CD
    checks {
        uncommittedChanges.set(false)
        aheadOfRemote.set(false)
    }
}
```

## Troubleshooting

### Проблема: Версия всегда SNAPSHOT
**Решение**: Создайте git тег и находитесь на нём:
```bash
git tag v1.3.0
git checkout v1.3.0
./gradlew currentVersion  # 1.3.0
```

### Проблема: Версия не меняется
**Решение**: Убедитесь, что теги запушены:
```bash
git push origin --tags
```

### Проблема: Неправильная версия в release ветке
**Решение**: Убедитесь, что release ветка содержит нужный тег:
```bash
git checkout release
git merge v1.3.0
```

## Best Practices

1. **Всегда создавайте теги для релизов**
2. **Используйте префикс `v` для тегов** (v1.0.0, v1.1.0)
3. **Публикуйте только с тегов или release ветки**
4. **Не публикуйте SNAPSHOT версии**
5. **Следуйте Semantic Versioning**

## CI/CD интеграция

GitHub Actions автоматически определяет версию при публикации:

```yaml
- name: Get version
  run: |
    VERSION=$(./gradlew currentVersion -q | grep "Project version:" | sed 's/Project version: //')
    echo "Publishing version: $VERSION"
```