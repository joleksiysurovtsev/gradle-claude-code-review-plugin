# 🚀 Автоматический релиз

## Как это работает

После настройки полной автоматизации, весь процесс релиза происходит автоматически при пуше в ветку `release`.

### Процесс релиза

1. **Делаете изменения в коде**
2. **Коммитите и пушите в ветку `release`**
3. **Всё остальное происходит автоматически:**
   - ✅ Определяется тип версии (patch/minor/major)
   - ✅ Инкрементируется версия
   - ✅ Запускаются тесты
   - ✅ Валидируется плагин
   - ✅ Собирается проект
   - ✅ Создаётся git тег
   - ✅ Создаётся GitHub Release
   - ✅ Публикуется в Gradle Plugin Portal

### Управление версиями

Версия автоматически определяется по тексту коммита:

#### Patch версия (1.2.3 → 1.2.4) - по умолчанию
```bash
git commit -m "Fix bug in review logic"
git commit -m "Update dependencies"
```

#### Minor версия (1.2.3 → 1.3.0)
```bash
git commit -m "[minor] Add new feature for batch reviews"
git commit -m "feat: Add support for custom prompts"
```

#### Major версия (1.2.3 → 2.0.0)
```bash
git commit -m "[major] Complete rewrite of review engine"
git commit -m "BREAKING CHANGE: New API format"
```

### Пример полного процесса

```bash
# 1. Переключаемся на ветку release
git checkout release

# 2. Делаем изменения
# ... редактируем файлы ...

# 3. Коммитим с нужным префиксом
git add .
git commit -m "Fix review timeout issue"  # Это создаст patch версию

# 4. Пушим в release ветку
git push origin release

# 5. ВСЁ! Дальше происходит автоматически:
#    - Версия увеличится с 1.2.4 до 1.2.5
#    - Создастся тег v1.2.5
#    - Запустятся тесты
#    - Создастся GitHub Release
#    - Опубликуется в Gradle Plugin Portal
```

### Мониторинг процесса

Следите за процессом на странице GitHub Actions:
https://github.com/joleksiysurovtsev/gradle-claude-code-review-plugin/actions

### Требования

Для работы автоматизации необходимо:
- ✅ Секреты `GRADLE_PUBLISH_KEY` и `GRADLE_PUBLISH_SECRET` в настройках репозитория
- ✅ Ветка `release` должна существовать
- ✅ Права на создание релизов в репозитории

### Отключение автоматизации

Если нужно временно отключить автоматический релиз:
1. Отключите workflow в настройках Actions
2. Или добавьте `[skip-release]` в сообщение коммита

### Ручной релиз

Если нужно создать релиз вручную:
1. Используйте workflow "Bump Version" через GitHub UI
2. Или создайте тег локально:
   ```bash
   git tag v1.2.5
   git push origin v1.2.5
   ```