#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Script para avaliar modelos existentes e identificar problemas de classificação
"""

import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
from sklearn.metrics import classification_report, confusion_matrix
import tensorflow as tf
from tensorflow.keras.models import load_model
from tensorflow.keras.utils import to_categorical
import argparse
import os


def load_and_preprocess_data(csv_path, model_type='numbers'):
    """Carrega e preprocessa dados do CSV"""
    print(f"Carregando dados de {csv_path}...")
    
    df = pd.read_csv(csv_path, header=None)
    
    # Separar features e labels
    y = df.iloc[:, 0].values
    x = df.iloc[:, 1:].values
    
    # Ajustar labels para letras (EMNIST começa de 1)
    if model_type == 'letters':
        y = y - 1
        num_classes = 26
    else:
        num_classes = 10
    
    # Reshape para formato de imagem
    x = x.reshape(-1, 28, 28, 1).astype('float32') / 255.0
    y = to_categorical(y, num_classes)
    
    print(f"Dados carregados: {x.shape}")
    return x, y, num_classes


def evaluate_keras_model(model_path, x_test, y_test, model_type='numbers'):
    """Avalia modelo Keras (.h5)"""
    print(f"\nAvaliando modelo Keras: {model_path}")
    
    # Carregar modelo
    model = load_model(model_path)
    model.summary()
    
    # Avaliar
    test_loss, test_acc = model.evaluate(x_test, y_test, verbose=0)
    print(f"\nAcurácia no teste: {test_acc:.4f}")
    print(f"Loss no teste: {test_loss:.4f}")
    
    # Predições
    y_pred = model.predict(x_test)
    y_pred_classes = np.argmax(y_pred, axis=1)
    y_true_classes = np.argmax(y_test, axis=1)
    
    # Labels
    if model_type == 'numbers':
        target_names = [str(i) for i in range(10)]
    else:
        target_names = [chr(65 + i) for i in range(26)]
    
    # Relatório de classificação
    print("\nRelatório de Classificação:")
    print(classification_report(y_true_classes, y_pred_classes, 
                               target_names=target_names))
    
    # Matriz de confusão
    cm = confusion_matrix(y_true_classes, y_pred_classes)
    
    # Plotar matriz de confusão
    plt.figure(figsize=(12, 10))
    sns.heatmap(cm, annot=True, fmt='d', cmap='Blues',
               xticklabels=target_names,
               yticklabels=target_names)
    plt.title(f'Matriz de Confusão - {model_type.capitalize()}')
    plt.ylabel('Rótulo Verdadeiro')
    plt.xlabel('Rótulo Predito')
    plt.tight_layout()
    
    output_name = f'evaluation_confusion_matrix_{model_type}.png'
    plt.savefig(output_name, dpi=150)
    print(f"\nMatriz de confusão salva em: {output_name}")
    
    # Identificar pares mais confundidos
    print("\nTop 10 pares de caracteres mais confundidos:")
    confused_pairs = []
    for i in range(len(cm)):
        for j in range(len(cm)):
            if i != j and cm[i][j] > 0:
                confused_pairs.append((target_names[i], target_names[j], cm[i][j]))
    
    confused_pairs.sort(key=lambda x: x[2], reverse=True)
    for idx, (true_label, pred_label, count) in enumerate(confused_pairs[:10], 1):
        print(f"  {idx}. {true_label} → {pred_label}: {count} vezes ({count/len(y_test)*100:.2f}%)")
    
    # Análise por classe
    print("\nAcurácia por classe:")
    for i, label in enumerate(target_names):
        correct = cm[i][i]
        total = cm[i].sum()
        acc = correct / total * 100 if total > 0 else 0
        print(f"  {label}: {acc:.2f}% ({correct}/{total})")
    
    return test_acc, cm


def evaluate_tflite_model(model_path, x_test, y_test, model_type='numbers'):
    """Avalia modelo TFLite (.tflite)"""
    print(f"\nAvaliando modelo TFLite: {model_path}")
    
    # Carregar modelo TFLite
    interpreter = tf.lite.Interpreter(model_path=model_path)
    interpreter.allocate_tensors()
    
    # Obter detalhes de entrada e saída
    input_details = interpreter.get_input_details()
    output_details = interpreter.get_output_details()
    
    print(f"Input shape: {input_details[0]['shape']}")
    print(f"Output shape: {output_details[0]['shape']}")
    
    # Fazer predições
    predictions = []
    for i in range(len(x_test)):
        # Preparar entrada
        input_data = x_test[i:i+1].astype(np.float32)
        interpreter.set_tensor(input_details[0]['index'], input_data)
        
        # Executar inferência
        interpreter.invoke()
        
        # Obter saída
        output_data = interpreter.get_tensor(output_details[0]['index'])
        predictions.append(output_data[0])
        
        if (i + 1) % 1000 == 0:
            print(f"Processado {i+1}/{len(x_test)} amostras...")
    
    predictions = np.array(predictions)
    y_pred_classes = np.argmax(predictions, axis=1)
    y_true_classes = np.argmax(y_test, axis=1)
    
    # Calcular acurácia
    accuracy = np.mean(y_pred_classes == y_true_classes)
    print(f"\nAcurácia no teste: {accuracy:.4f}")
    
    # Labels
    if model_type == 'numbers':
        target_names = [str(i) for i in range(10)]
    else:
        target_names = [chr(65 + i) for i in range(26)]
    
    # Relatório de classificação
    print("\nRelatório de Classificação:")
    print(classification_report(y_true_classes, y_pred_classes,
                               target_names=target_names))
    
    # Matriz de confusão
    cm = confusion_matrix(y_true_classes, y_pred_classes)
    
    # Plotar
    plt.figure(figsize=(12, 10))
    sns.heatmap(cm, annot=True, fmt='d', cmap='Reds',
               xticklabels=target_names,
               yticklabels=target_names)
    plt.title(f'Matriz de Confusão TFLite - {model_type.capitalize()}')
    plt.ylabel('Rótulo Verdadeiro')
    plt.xlabel('Rótulo Predito')
    plt.tight_layout()
    
    output_name = f'evaluation_tflite_confusion_matrix_{model_type}.png'
    plt.savefig(output_name, dpi=150)
    print(f"\nMatriz de confusão salva em: {output_name}")
    
    return accuracy, cm


def compare_models(keras_path, tflite_path, x_test, y_test, model_type='numbers'):
    """Compara modelos Keras e TFLite"""
    print("\n" + "="*60)
    print("COMPARAÇÃO DE MODELOS")
    print("="*60)
    
    # Avaliar Keras
    keras_acc, keras_cm = evaluate_keras_model(keras_path, x_test, y_test, model_type)
    
    # Avaliar TFLite
    tflite_acc, tflite_cm = evaluate_tflite_model(tflite_path, x_test, y_test, model_type)
    
    # Comparar
    print("\n" + "="*60)
    print("RESUMO DA COMPARAÇÃO")
    print("="*60)
    print(f"Acurácia Keras:  {keras_acc:.4f}")
    print(f"Acurácia TFLite: {tflite_acc:.4f}")
    print(f"Diferença:       {abs(keras_acc - tflite_acc):.4f}")
    
    if abs(keras_acc - tflite_acc) < 0.01:
        print("✓ Modelos são equivalentes (diferença < 1%)")
    else:
        print("⚠ Modelos têm diferença significativa")


def main():
    parser = argparse.ArgumentParser(description='Avaliar modelos CNN existentes')
    parser.add_argument('--model', type=str, required=True,
                       help='Caminho para o modelo (.h5 ou .tflite)')
    parser.add_argument('--test-data', type=str, required=True,
                       help='Caminho para dados de teste (CSV)')
    parser.add_argument('--type', type=str, choices=['numbers', 'letters'],
                       required=True, help='Tipo de modelo')
    parser.add_argument('--compare-tflite', type=str,
                       help='Caminho para modelo TFLite para comparação')
    
    args = parser.parse_args()
    
    # Carregar dados de teste
    x_test, y_test, num_classes = load_and_preprocess_data(
        args.test_data, args.type
    )
    
    # Verificar extensão do modelo
    if args.model.endswith('.h5'):
        if args.compare_tflite:
            compare_models(args.model, args.compare_tflite, 
                         x_test, y_test, args.type)
        else:
            evaluate_keras_model(args.model, x_test, y_test, args.type)
    elif args.model.endswith('.tflite'):
        evaluate_tflite_model(args.model, x_test, y_test, args.type)
    else:
        print("Erro: Formato de modelo não suportado. Use .h5 ou .tflite")


if __name__ == '__main__':
    main()
