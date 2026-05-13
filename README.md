# CompatControl

App Android para controlar flags de compatibilidade por app via root (libsu) com suporte a LSPosed.

## Funcionalidades

- Lista todos os apps instalados
- Controle de escala de renderização (30% a 100%) por app
- Flags de compatibilidade: Aspect Ratio, Display APIs
- Flags de memória: Non-Resize, Nav Insets
- Hook LSPosed para aplicar configs automaticamente quando o app abre
- Requer root (KernelSU, Magisk)

## Setup do GitHub Actions

### 1. Gerar Keystore

```bash
keytool -genkey -v -keystore compatcontrol.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias compatcontrol
```

### 2. Converter pra Base64

```bash
# Linux/Mac
base64 -i compatcontrol.jks | tr -d '\n'

# Windows (PowerShell)
[Convert]::ToBase64String([IO.File]::ReadAllBytes("compatcontrol.jks"))
```

### 3. Adicionar Secrets no GitHub

Vá em **Settings → Secrets and variables → Actions** e adicione:

| Secret | Valor |
|--------|-------|
| `KEYSTORE_BASE64` | Output do comando base64 |
| `KEYSTORE_PASSWORD` | Senha da keystore |
| `KEY_ALIAS` | Alias da key (ex: `compatcontrol`) |
| `KEY_PASSWORD` | Senha da key |

### 4. Push na main

O APK é gerado automaticamente a cada push na branch `main` e fica disponível em:
- **Actions → Build Release APK → Artifacts**
- **Releases** (criado automaticamente)

## Configurar LSPosed

1. Instale o APK
2. Abra o LSPosed Manager
3. Ative o módulo CompatControl
4. Selecione os apps que quer hookear no escopo
5. Reinicie o sistema

## Requisitos

- Android 12+ (minSdk 31)
- Root (KernelSU ou Magisk)
- LSPosed (opcional, para hooks automáticos)
