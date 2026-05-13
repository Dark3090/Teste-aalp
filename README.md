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
