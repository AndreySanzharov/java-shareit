package ru.practicum.shareit.user.service;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.SameParametersExistsException;
import ru.practicum.shareit.exception.ObjectDtoException;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.util.*;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;
    private final UserRepository userRepository;

    public UserDto add(@Valid UserDto userDto) {
        validateUserDto(userDto);
        User user = userMapper.fromUserDto(userDto);
        user = userRepository.save(user);
        return userMapper.toUserDto(user);
    }

    @Override
    public UserDto update(int userId, UserDto userDto) {
        validateUserById(userId);

        User user = userRepository.findById(userId).get();

        if (userDto.getEmail() != null) {
            if (!Objects.equals(userDto.getEmail(), user.getEmail()) && userWithEmailExists(userDto.getEmail())) {
                throw new SameParametersExistsException("Пользователя с почтой " + userDto.getEmail() + " не существует.");
            }
            user.setEmail(userDto.getEmail());
        }

        if (userDto.getName() != null) {
            user.setName(userDto.getName());
        }

        user = userRepository.save(user);

        return userMapper.toUserDto(user);
    }

    @Override
    public UserDto get(int id) {
        validateUserById(id);
        Optional<User> user = userRepository.findById(id);
        return user.map(userMapper::toUserDto).orElse(null);
    }

    @Override
    public void delete(int id) {
        validateUserById(id);
        userRepository.deleteById(id);
    }

    private void validateUserDto(UserDto userDto) {
        if (userDto.getName() == null || userDto.getEmail() == null) {
            throw new ObjectDtoException("Имя и почта не должны быть пустыми.");
        }
    }

    private void validateUserById(Integer userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователя с id = " + userId + " не существует.");
        }
    }

    private boolean userWithEmailExists(String email) {
        return userRepository.findAll().stream().map(User::getEmail).anyMatch(email::equals);
    }
}