#!/bin/bash
# Script conveniente para treinar modelos CNN melhorados

set -e  # Parar em caso de erro

echo "======================================"
echo "  Treinamento de Modelos CNN"
echo "======================================"
echo ""

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Verificar se Python está instalado
if ! command -v python3 &> /dev/null; then
    echo -e "${RED}Erro: Python 3 não encontrado${NC}"
    echo "Instale Python 3 primeiro: https://www.python.org/downloads/"
    exit 1
fi

# Verificar se requirements estão instalados
echo -e "${YELLOW}Verificando dependências...${NC}"
if ! python3 -c "import tensorflow" 2>/dev/null; then
    echo -e "${YELLOW}Instalando dependências...${NC}"
    pip3 install -r requirements.txt
fi

echo -e "${GREEN}✓ Dependências OK${NC}"
echo ""

# Função para mostrar uso
show_usage() {
    echo "Uso: ./train.sh [opções]"
    echo ""
    echo "Opções:"
    echo "  --type TYPE              Tipo de modelo: numbers, letters, ou both (padrão: both)"
    echo "  --train-numbers PATH     Caminho para CSV de treino de números"
    echo "  --test-numbers PATH      Caminho para CSV de teste de números"
    echo "  --train-letters PATH     Caminho para CSV de treino de letras"
    echo "  --test-letters PATH      Caminho para CSV de teste de letras"
    echo "  --epochs N               Número de epochs (padrão: 50)"
    echo "  --batch-size N           Tamanho do batch (padrão: 128)"
    echo "  --quick                  Modo rápido: usa amostra pequena para teste"
    echo ""
    echo "Exemplos:"
    echo "  ./train.sh --type numbers --train-numbers data/train.csv --test-numbers data/test.csv"
    echo "  ./train.sh --type both --train-numbers data/num_train.csv --test-numbers data/num_test.csv \\"
    echo "             --train-letters data/let_train.csv --test-letters data/let_test.csv"
    echo "  ./train.sh --quick  # Teste rápido com dados de exemplo"
    echo ""
}

# Valores padrão
TYPE="both"
EPOCHS=50
BATCH_SIZE=128
QUICK_MODE=false

# Parse argumentos
while [[ $# -gt 0 ]]; do
    case $1 in
        --type)
            TYPE="$2"
            shift 2
            ;;
        --train-numbers)
            TRAIN_NUMBERS="$2"
            shift 2
            ;;
        --test-numbers)
            TEST_NUMBERS="$2"
            shift 2
            ;;
        --train-letters)
            TRAIN_LETTERS="$2"
            shift 2
            ;;
        --test-letters)
            TEST_LETTERS="$2"
            shift 2
            ;;
        --epochs)
            EPOCHS="$2"
            shift 2
            ;;
        --batch-size)
            BATCH_SIZE="$2"
            shift 2
            ;;
        --quick)
            QUICK_MODE=true
            shift
            ;;
        --help|-h)
            show_usage
            exit 0
            ;;
        *)
            echo -e "${RED}Opção desconhecida: $1${NC}"
            show_usage
            exit 1
            ;;
    esac
done

# Modo rápido - criar dados de exemplo
if [ "$QUICK_MODE" = true ]; then
    echo -e "${YELLOW}Modo rápido ativado - usando dados de exemplo${NC}"
    echo "NOTA: Isso é apenas para testar o script. Para resultados reais, use dados completos."
    echo ""
    
    # Aqui você poderia baixar dados de exemplo ou usar dados existentes
    echo -e "${RED}Modo rápido requer dados de exemplo pré-configurados${NC}"
    echo "Por favor, forneça os caminhos dos arquivos CSV manualmente."
    exit 1
fi

# Construir comando
CMD="python3 train_improved_cnn.py --type $TYPE --epochs $EPOCHS --batch-size $BATCH_SIZE"

# Adicionar caminhos de arquivos
if [ "$TYPE" = "numbers" ] || [ "$TYPE" = "both" ]; then
    if [ -z "$TRAIN_NUMBERS" ] || [ -z "$TEST_NUMBERS" ]; then
        echo -e "${RED}Erro: Para treinar números, forneça --train-numbers e --test-numbers${NC}"
        show_usage
        exit 1
    fi
    CMD="$CMD --train-numbers $TRAIN_NUMBERS --test-numbers $TEST_NUMBERS"
fi

if [ "$TYPE" = "letters" ] || [ "$TYPE" = "both" ]; then
    if [ -z "$TRAIN_LETTERS" ] || [ -z "$TEST_LETTERS" ]; then
        echo -e "${RED}Erro: Para treinar letras, forneça --train-letters e --test-letters${NC}"
        show_usage
        exit 1
    fi
    CMD="$CMD --train-letters $TRAIN_LETTERS --test-letters $TEST_LETTERS"
fi

# Mostrar configuração
echo "======================================"
echo "  Configuração de Treinamento"
echo "======================================"
echo "Tipo: $TYPE"
echo "Epochs: $EPOCHS"
echo "Batch Size: $BATCH_SIZE"
if [ ! -z "$TRAIN_NUMBERS" ]; then
    echo "Treino Números: $TRAIN_NUMBERS"
    echo "Teste Números: $TEST_NUMBERS"
fi
if [ ! -z "$TRAIN_LETTERS" ]; then
    echo "Treino Letras: $TRAIN_LETTERS"
    echo "Teste Letras: $TEST_LETTERS"
fi
echo "======================================"
echo ""

# Confirmar
read -p "Iniciar treinamento? (s/N) " -n 1 -r
echo
if [[ ! $REPLY =~ ^[SsYy]$ ]]; then
    echo "Treinamento cancelado."
    exit 0
fi

# Executar treinamento
echo ""
echo -e "${GREEN}Iniciando treinamento...${NC}"
echo "Comando: $CMD"
echo ""

eval $CMD

# Verificar sucesso
if [ $? -eq 0 ]; then
    echo ""
    echo "======================================"
    echo -e "${GREEN}  ✓ Treinamento Concluído!${NC}"
    echo "======================================"
    echo ""
    echo "Arquivos gerados:"
    
    if [ "$TYPE" = "numbers" ] || [ "$TYPE" = "both" ]; then
        echo "  • cnn_numbers_improved.h5"
        echo "  • app/src/main/assets/cnn_numbers.tflite"
        echo "  • confusion_matrix_numbers.png"
        echo "  • training_history_numbers.png"
    fi
    
    if [ "$TYPE" = "letters" ] || [ "$TYPE" = "both" ]; then
        echo "  • cnn_letters_improved.h5"
        echo "  • app/src/main/assets/cnn_letters.tflite"
        echo "  • confusion_matrix_letters.png"
        echo "  • training_history_letters.png"
    fi
    
    echo ""
    echo "Próximos passos:"
    echo "  1. Revise as matrizes de confusão (arquivos .png)"
    echo "  2. Compile o app Android: ./gradlew assembleDebug"
    echo "  3. Teste no dispositivo!"
    echo ""
else
    echo ""
    echo "======================================"
    echo -e "${RED}  ✗ Erro no Treinamento${NC}"
    echo "======================================"
    echo ""
    echo "Verifique os logs acima para detalhes do erro."
    exit 1
fi
