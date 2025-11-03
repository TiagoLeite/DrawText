#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Script para converter modelos Keras existentes para TFLite compatível
com versões antigas do TensorFlow Lite no Android
"""

import tensorflow as tf
from tensorflow.keras.models import load_model
import argparse
import os


def convert_to_compatible_tflite(keras_model_path, output_path):
    """
    Converte modelo Keras para TFLite com compatibilidade retroativa
    
    Args:
        keras_model_path: Caminho para o modelo .h5
        output_path: Caminho para salvar o .tflite
    """
    print(f"Carregando modelo: {keras_model_path}")
    model = load_model(keras_model_path)
    
    print("Resumo do modelo:")
    model.summary()
    
    print("\nConvertendo para TFLite compatível...")
    
    # Criar conversor
    converter = tf.lite.TFLiteConverter.from_keras_model(model)
    
    # CONFIGURAÇÕES PARA COMPATIBILIDADE
    # Usar apenas operadores built-in do TFLite (versões antigas)
    converter.target_spec.supported_ops = [
        tf.lite.OpsSet.TFLITE_BUILTINS,
    ]
    
    # Desabilitar otimizações que podem gerar operadores mais novos
    # NÃO usar: converter.optimizations = [tf.lite.Optimize.DEFAULT]
    
    # Garantir compatibilidade
    converter._experimental_lower_tensor_list_ops = False
    
    # Converter
    try:
        tflite_model = converter.convert()
        print("✓ Conversão bem-sucedida!")
    except Exception as e:
        print(f"Erro na conversão: {e}")
        print("\nTentando método alternativo...")
        
        # Método alternativo: converter sem nenhuma configuração especial
        converter = tf.lite.TFLiteConverter.from_keras_model(model)
        tflite_model = converter.convert()
        print("✓ Conversão alternativa bem-sucedida!")
    
    # Salvar
    with open(output_path, 'wb') as f:
        f.write(tflite_model)
    
    file_size_kb = os.path.getsize(output_path) / 1024
    print(f"\n✓ Modelo salvo em: {output_path}")
    print(f"  Tamanho: {file_size_kb:.2f} KB")
    print(f"  Compatível com versões antigas do TFLite")
    
    return output_path


def verify_tflite_model(tflite_path):
    """
    Verifica se o modelo TFLite pode ser carregado
    
    Args:
        tflite_path: Caminho para o arquivo .tflite
    """
    print(f"\nVerificando modelo: {tflite_path}")
    
    try:
        # Tentar carregar o modelo
        interpreter = tf.lite.Interpreter(model_path=tflite_path)
        interpreter.allocate_tensors()
        
        # Obter detalhes
        input_details = interpreter.get_input_details()
        output_details = interpreter.get_output_details()
        
        print("✓ Modelo carregado com sucesso!")
        print(f"\nDetalhes do modelo:")
        print(f"  Input shape: {input_details[0]['shape']}")
        print(f"  Input dtype: {input_details[0]['dtype']}")
        print(f"  Output shape: {output_details[0]['shape']}")
        print(f"  Output dtype: {output_details[0]['dtype']}")
        
        return True
    except Exception as e:
        print(f"✗ Erro ao carregar modelo: {e}")
        return False


def main():
    parser = argparse.ArgumentParser(
        description='Converter modelos Keras para TFLite compatível com Android',
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
Exemplos de uso:

1. Converter modelo de números:
   python convert_to_compatible_tflite.py \\
       --input cnn_numbers_improved.h5 \\
       --output app/src/main/assets/cnn_numbers.tflite

2. Converter modelo de letras:
   python convert_to_compatible_tflite.py \\
       --input cnn_letters_improved.h5 \\
       --output app/src/main/assets/cnn_letters.tflite

3. Converter e verificar:
   python convert_to_compatible_tflite.py \\
       --input cnn_numbers_improved.h5 \\
       --output cnn_numbers.tflite \\
       --verify
        """
    )
    
    parser.add_argument('--input', '-i', type=str, required=True,
                       help='Caminho para o modelo Keras (.h5)')
    parser.add_argument('--output', '-o', type=str, required=True,
                       help='Caminho para salvar o modelo TFLite (.tflite)')
    parser.add_argument('--verify', '-v', action='store_true',
                       help='Verificar o modelo após conversão')
    
    args = parser.parse_args()
    
    # Verificar se arquivo de entrada existe
    if not os.path.exists(args.input):
        print(f"Erro: Arquivo não encontrado: {args.input}")
        return
    
    # Criar diretório de saída se não existir
    output_dir = os.path.dirname(args.output)
    if output_dir and not os.path.exists(output_dir):
        os.makedirs(output_dir)
        print(f"Criado diretório: {output_dir}")
    
    # Converter
    print("="*60)
    print("CONVERSÃO PARA TFLITE COMPATÍVEL")
    print("="*60)
    
    output_path = convert_to_compatible_tflite(args.input, args.output)
    
    # Verificar se solicitado
    if args.verify:
        print("\n" + "="*60)
        print("VERIFICAÇÃO DO MODELO")
        print("="*60)
        verify_tflite_model(output_path)
    
    print("\n" + "="*60)
    print("CONCLUÍDO!")
    print("="*60)
    print(f"\nPróximos passos:")
    print(f"1. Copie o arquivo para o app Android (se ainda não estiver lá)")
    print(f"2. Compile o app: ./gradlew assembleDebug")
    print(f"3. Instale: adb install app/build/outputs/apk/debug/app-debug.apk")


if __name__ == '__main__':
    main()
