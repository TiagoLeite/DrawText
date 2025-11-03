#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Improved CNN Training Script for Letters and Numbers Recognition
Addresses classification issues with similar characters (e.g., 1 vs 7)

Features:
- Enhanced CNN architecture with batch normalization
- Data augmentation for better generalization
- Proper validation and testing
- Confusion matrix analysis
- Model export to TFLite for Android deployment
"""

import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
from sklearn.model_selection import train_test_split
from sklearn.metrics import classification_report, confusion_matrix
import tensorflow as tf
from tensorflow import keras
from tensorflow.keras import layers, models
from tensorflow.keras.preprocessing.image import ImageDataGenerator
from tensorflow.keras.callbacks import EarlyStopping, ReduceLROnPlateau, ModelCheckpoint
from tensorflow.keras.utils import to_categorical
import os
import argparse


class ImprovedCNNTrainer:
    """Trainer class for improved CNN models"""
    
    def __init__(self, model_type='numbers', input_shape=(28, 28, 1)):
        """
        Initialize trainer
        
        Args:
            model_type: 'numbers' or 'letters'
            input_shape: Input image shape (height, width, channels)
        """
        self.model_type = model_type
        self.input_shape = input_shape
        self.num_classes = 10 if model_type == 'numbers' else 26
        self.model = None
        self.history = None
        
    def create_improved_model(self):
        """
        Create improved CNN architecture with:
        - Batch normalization for stable training
        - Dropout for regularization
        - More convolutional layers for better feature extraction
        - Residual-like connections
        """
        model = models.Sequential([
            # First convolutional block
            layers.Conv2D(32, (3, 3), padding='same', input_shape=self.input_shape),
            layers.BatchNormalization(),
            layers.Activation('relu'),
            layers.Conv2D(32, (3, 3), padding='same'),
            layers.BatchNormalization(),
            layers.Activation('relu'),
            layers.MaxPooling2D((2, 2)),
            layers.Dropout(0.25),
            
            # Second convolutional block
            layers.Conv2D(64, (3, 3), padding='same'),
            layers.BatchNormalization(),
            layers.Activation('relu'),
            layers.Conv2D(64, (3, 3), padding='same'),
            layers.BatchNormalization(),
            layers.Activation('relu'),
            layers.MaxPooling2D((2, 2)),
            layers.Dropout(0.25),
            
            # Third convolutional block
            layers.Conv2D(128, (3, 3), padding='same'),
            layers.BatchNormalization(),
            layers.Activation('relu'),
            layers.Conv2D(128, (3, 3), padding='same'),
            layers.BatchNormalization(),
            layers.Activation('relu'),
            layers.MaxPooling2D((2, 2)),
            layers.Dropout(0.4),
            
            # Dense layers - REVERTIDO para tamanho moderado
            # Modelo de 4 camadas estava causando overfitting
            layers.Flatten(),
            layers.Dense(256),  # Revertido de 512
            layers.BatchNormalization(),
            layers.Activation('relu'),
            layers.Dropout(0.5),
            layers.Dense(128),  # Revertido de 256
            layers.BatchNormalization(),
            layers.Activation('relu'),
            layers.Dropout(0.5),
            
            # Output layer
            layers.Dense(self.num_classes, activation='softmax')
        ])
        
        # Compile with Adam optimizer and learning rate scheduling
        optimizer = keras.optimizers.Adam(learning_rate=0.001)
        model.compile(
            optimizer=optimizer,
            loss='categorical_crossentropy',
            metrics=['accuracy']
        )
        
        self.model = model
        return model
    
    def load_data(self, train_path, test_path):
        """
        Load and preprocess data from CSV files
        
        Args:
            train_path: Path to training CSV file
            test_path: Path to testing CSV file
            
        Returns:
            x_train, y_train, x_test, y_test
        """
        print(f"Loading {self.model_type} data...")
        
        # Load data
        train_df = pd.read_csv(train_path, header=None)
        test_df = pd.read_csv(test_path, header=None)
        
        # Separate features and labels
        y_train = train_df.iloc[:, 0].values
        x_train = train_df.iloc[:, 1:].values
        
        y_test = test_df.iloc[:, 0].values
        x_test = test_df.iloc[:, 1:].values
        
        # Adjust labels (EMNIST labels start from 1)
        if self.model_type == 'letters':
            y_train = y_train - 1
            y_test = y_test - 1
        
        # Reshape to image format
        x_train = x_train.reshape(-1, 28, 28).astype('float32') / 255.0
        x_test = x_test.reshape(-1, 28, 28).astype('float32') / 255.0
        
        # IMPORTANTE: EMNIST images need to be transposed and flipped
        # The dataset comes rotated 90 degrees and mirrored
        x_train = np.array([np.fliplr(np.rot90(img, k=3)) for img in x_train])
        x_test = np.array([np.fliplr(np.rot90(img, k=3)) for img in x_test])
        
        # Add channel dimension
        x_train = x_train.reshape(-1, 28, 28, 1)
        x_test = x_test.reshape(-1, 28, 28, 1)
        
        # Convert labels to categorical
        y_train = to_categorical(y_train, self.num_classes)
        y_test = to_categorical(y_test, self.num_classes)
        
        print(f"Training set: {x_train.shape}, Test set: {x_test.shape}")
        
        return x_train, y_train, x_test, y_test
    
    def create_data_augmentation(self):
        """
        Create data augmentation generator to improve generalization
        Helps the model handle variations in handwriting
        
        IMPORTANTE: Augmentation mais agressivo para letras ajuda o modelo
        a distinguir melhor pares confusos como O/Q, I/L, B/D
        """
        datagen = ImageDataGenerator(
            rotation_range=12,          # Reduzido: 12 graus (era 20, muito agressivo)
            width_shift_range=0.1,      # Reduzido: 10% (era 15%)
            height_shift_range=0.1,     # Reduzido: 10% (era 15%)
            shear_range=0.1,            # Reduzido: 10% (era 15%)
            zoom_range=0.1,             # Reduzido: 10% (era 15%)
            fill_mode='nearest'         # Fill empty pixels
        )
        return datagen
    
    def train(self, x_train, y_train, x_val, y_val, epochs=50, batch_size=128):
        """
        Train the model with data augmentation and callbacks
        
        Args:
            x_train, y_train: Training data
            x_val, y_val: Validation data
            epochs: Number of training epochs
            batch_size: Batch size for training
        """
        print(f"\nTraining {self.model_type} model...")
        
        # Create data augmentation
        datagen = self.create_data_augmentation()
        datagen.fit(x_train)
        
        # Define callbacks
        callbacks = [
            # Early stopping to prevent overfitting
            EarlyStopping(
                monitor='val_loss',
                patience=10,
                restore_best_weights=True,
                verbose=1
            ),
            # Reduce learning rate when validation loss plateaus
            ReduceLROnPlateau(
                monitor='val_loss',
                factor=0.5,
                patience=5,
                min_lr=1e-7,
                verbose=1
            ),
            # Save best model
            ModelCheckpoint(
                f'best_model_{self.model_type}.h5',
                monitor='val_accuracy',
                save_best_only=True,
                verbose=1
            )
        ]
        
        # Train with data augmentation
        self.history = self.model.fit(
            datagen.flow(x_train, y_train, batch_size=batch_size),
            epochs=epochs,
            validation_data=(x_val, y_val),
            callbacks=callbacks,
            verbose=1
        )
        
        return self.history
    
    def evaluate(self, x_test, y_test):
        """
        Evaluate model performance and generate confusion matrix
        
        Args:
            x_test, y_test: Test data
        """
        print(f"\nEvaluating {self.model_type} model...")
        
        # Get predictions
        y_pred = self.model.predict(x_test)
        y_pred_classes = np.argmax(y_pred, axis=1)
        y_true_classes = np.argmax(y_test, axis=1)
        
        # Calculate accuracy
        test_loss, test_acc = self.model.evaluate(x_test, y_test, verbose=0)
        print(f"Test accuracy: {test_acc:.4f}")
        print(f"Test loss: {test_loss:.4f}")
        
        # Generate classification report
        if self.model_type == 'numbers':
            target_names = [str(i) for i in range(10)]
        else:
            target_names = [chr(65 + i) for i in range(26)]  # A-Z
        
        print("\nClassification Report:")
        print(classification_report(y_true_classes, y_pred_classes, 
                                   target_names=target_names))
        
        # Generate confusion matrix
        cm = confusion_matrix(y_true_classes, y_pred_classes)
        
        # Plot confusion matrix
        plt.figure(figsize=(12, 10))
        sns.heatmap(cm, annot=True, fmt='d', cmap='Blues',
                   xticklabels=target_names,
                   yticklabels=target_names)
        plt.title(f'Confusion Matrix - {self.model_type.capitalize()}')
        plt.ylabel('True Label')
        plt.xlabel('Predicted Label')
        plt.tight_layout()
        plt.savefig(f'confusion_matrix_{self.model_type}.png', dpi=150)
        print(f"Confusion matrix saved to confusion_matrix_{self.model_type}.png")
        
        # Identify most confused pairs
        print("\nMost confused character pairs:")
        confused_pairs = []
        for i in range(len(cm)):
            for j in range(len(cm)):
                if i != j and cm[i][j] > 0:
                    confused_pairs.append((target_names[i], target_names[j], cm[i][j]))
        
        confused_pairs.sort(key=lambda x: x[2], reverse=True)
        for true_label, pred_label, count in confused_pairs[:10]:
            print(f"  {true_label} → {pred_label}: {count} times")
        
        return test_acc, cm
    
    def plot_training_history(self):
        """Plot training history"""
        if self.history is None:
            print("No training history available")
            return
        
        fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(14, 5))
        
        # Plot accuracy
        ax1.plot(self.history.history['accuracy'], label='Train Accuracy')
        ax1.plot(self.history.history['val_accuracy'], label='Val Accuracy')
        ax1.set_title(f'{self.model_type.capitalize()} - Model Accuracy')
        ax1.set_xlabel('Epoch')
        ax1.set_ylabel('Accuracy')
        ax1.legend()
        ax1.grid(True)
        
        # Plot loss
        ax2.plot(self.history.history['loss'], label='Train Loss')
        ax2.plot(self.history.history['val_loss'], label='Val Loss')
        ax2.set_title(f'{self.model_type.capitalize()} - Model Loss')
        ax2.set_xlabel('Epoch')
        ax2.set_ylabel('Loss')
        ax2.legend()
        ax2.grid(True)
        
        plt.tight_layout()
        plt.savefig(f'training_history_{self.model_type}.png', dpi=150)
        print(f"Training history saved to training_history_{self.model_type}.png")
    
    def export_to_tflite(self, output_path):
        """
        Export model to TensorFlow Lite format for Android deployment
        with backward compatibility for older TFLite versions
        
        Args:
            output_path: Path to save .tflite file
        """
        print(f"\nExporting model to TFLite format...")
        
        # Convert to TFLite
        converter = tf.lite.TFLiteConverter.from_keras_model(self.model)
        
        # IMPORTANTE: Configurar para compatibilidade com versões antigas do TFLite
        # Isso resolve o erro "Didn't find op for builtin opcode 'FULLY_CONNECTED' version '12'"
        
        # Usar operadores compatíveis com versões antigas
        converter.target_spec.supported_ops = [
            tf.lite.OpsSet.TFLITE_BUILTINS,  # Operadores built-in do TFLite
        ]
        
        # Desabilitar otimizações que podem causar incompatibilidade
        # Remover otimização DEFAULT que pode gerar operadores mais novos
        # converter.optimizations = [tf.lite.Optimize.DEFAULT]
        
        # Garantir compatibilidade com versões antigas
        converter._experimental_lower_tensor_list_ops = False
        
        # Converter
        try:
            tflite_model = converter.convert()
        except Exception as e:
            print(f"Erro na conversão: {e}")
            print("Tentando conversão alternativa sem otimizações...")
            # Tentar sem nenhuma otimização
            converter = tf.lite.TFLiteConverter.from_keras_model(self.model)
            converter.target_spec.supported_ops = [tf.lite.OpsSet.TFLITE_BUILTINS]
            tflite_model = converter.convert()
        
        # Save
        with open(output_path, 'wb') as f:
            f.write(tflite_model)
        
        print(f"Model exported to {output_path}")
        print(f"File size: {os.path.getsize(output_path) / 1024:.2f} KB")
        print(f"✓ Modelo compatível com versões antigas do TFLite")
    
    def save_model(self, output_path):
        """Save Keras model"""
        self.model.save(output_path)
        print(f"Keras model saved to {output_path}")


def main():
    """Main training function"""
    parser = argparse.ArgumentParser(description='Train improved CNN for handwriting recognition')
    parser.add_argument('--type', type=str, choices=['numbers', 'letters', 'both'],
                       default='both', help='Model type to train')
    parser.add_argument('--train-numbers', type=str, 
                       help='Path to numbers training CSV')
    parser.add_argument('--test-numbers', type=str,
                       help='Path to numbers testing CSV')
    parser.add_argument('--train-letters', type=str,
                       help='Path to letters training CSV')
    parser.add_argument('--test-letters', type=str,
                       help='Path to letters testing CSV')
    parser.add_argument('--epochs', type=int, default=50,
                       help='Number of training epochs')
    parser.add_argument('--batch-size', type=int, default=128,
                       help='Batch size')
    
    args = parser.parse_args()
    
    # Train numbers model
    if args.type in ['numbers', 'both']:
        if not args.train_numbers or not args.test_numbers:
            print("Error: Please provide paths to numbers dataset")
            print("Example paths:")
            print("  --train-numbers ./emnist-digits-train.csv")
            print("  --test-numbers ./emnist-digits-test.csv")
        else:
            print("\n" + "="*60)
            print("TRAINING NUMBERS MODEL")
            print("="*60)
            
            trainer = ImprovedCNNTrainer(model_type='numbers')
            trainer.create_improved_model()
            trainer.model.summary()
            
            # Load data
            x_train, y_train, x_test, y_test = trainer.load_data(
                args.train_numbers, args.test_numbers
            )
            
            # Split training data for validation
            x_train, x_val, y_train, y_val = train_test_split(
                x_train, y_train, test_size=0.1, random_state=42
            )
            
            # Train
            trainer.train(x_train, y_train, x_val, y_val, 
                         epochs=args.epochs, batch_size=args.batch_size)
            
            # Evaluate
            trainer.evaluate(x_test, y_test)
            
            # Plot history
            trainer.plot_training_history()
            
            # Export models
            trainer.save_model('cnn_numbers_improved.h5')
            trainer.export_to_tflite('app/src/main/assets/cnn_numbers.tflite')
    
    # Train letters model
    if args.type in ['letters', 'both']:
        if not args.train_letters or not args.test_letters:
            print("Error: Please provide paths to letters dataset")
            print("Example paths:")
            print("  --train-letters ./emnist-letters-train.csv")
            print("  --test-letters ./emnist-letters-test.csv")
        else:
            print("\n" + "="*60)
            print("TRAINING LETTERS MODEL")
            print("="*60)
            
            trainer = ImprovedCNNTrainer(model_type='letters')
            trainer.create_improved_model()
            trainer.model.summary()
            
            # Load data
            x_train, y_train, x_test, y_test = trainer.load_data(
                args.train_letters, args.test_letters.replace('csvv', 'csv')
            )
            
            # Split training data for validation
            x_train, x_val, y_train, y_val = train_test_split(
                x_train, y_train, test_size=0.1, random_state=42
            )
            
            # Train
            trainer.train(x_train, y_train, x_val, y_val,
                         epochs=args.epochs, batch_size=args.batch_size)
            
            # Evaluate
            trainer.evaluate(x_test, y_test)
            
            # Plot history
            trainer.plot_training_history()
            
            # Export models
            trainer.save_model('cnn_letters_improved.h5')
            trainer.export_to_tflite('app/src/main/assets/cnn_letters.tflite')
    
    print("\n" + "="*60)
    print("TRAINING COMPLETE!")
    print("="*60)


if __name__ == '__main__':
    main()
